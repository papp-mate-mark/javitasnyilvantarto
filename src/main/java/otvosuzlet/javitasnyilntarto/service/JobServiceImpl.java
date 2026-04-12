package otvosuzlet.javitasnyilntarto.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lowagie.text.Cell;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.alignment.HorizontalAlignment;
import com.lowagie.text.alignment.VerticalAlignment;
import com.lowagie.text.pdf.PdfWriter;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import net.coobird.thumbnailator.Thumbnails;
import otvosuzlet.javitasnyilntarto.dto.JobCompleteDTO;
import otvosuzlet.javitasnyilntarto.dto.JobPickedUpDTO;
import otvosuzlet.javitasnyilntarto.dto.JobSearchDto;
import otvosuzlet.javitasnyilntarto.dto.JobSearchJobDataDTO;
import otvosuzlet.javitasnyilntarto.enums.ImageType;
import otvosuzlet.javitasnyilntarto.exceptions.JobNotFoundException;
import otvosuzlet.javitasnyilntarto.exceptions.ValidationException;
import otvosuzlet.javitasnyilntarto.exceptions.ValidationException.ValidationError;
import otvosuzlet.javitasnyilntarto.model.Job;
import otvosuzlet.javitasnyilntarto.model.JobGroup;
import otvosuzlet.javitasnyilntarto.model.JobImage;
import otvosuzlet.javitasnyilntarto.model.Person;
import otvosuzlet.javitasnyilntarto.repository.JobRepository;
import otvosuzlet.javitasnyilntarto.specification.JobSearchSpec;

@Service
public class JobServiceImpl implements JobService {
    @Autowired
    private JobImageService imageService;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private Validator validator;

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void deleteJob(Integer id) {
        Job jobToDelete = findJobById(id);
        jobRepository.delete(jobToDelete);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void validateAndCompleteJob(Integer id, JobCompleteDTO jobCompleteData) {
        Job jobToUpdate = findJobById(id);

        Set<ConstraintViolation<JobCompleteDTO>> violations = validator.validate(jobCompleteData);

        LocalDateTime dateToSet = jobCompleteData.getDate();
        List<ValidationError> errors = new LinkedList<>(violations.stream()
            .map(violation -> new ValidationError(violation.getPropertyPath().toString(), violation.getMessage()))
            .toList());

        if (dateToSet == null) {
            dateToSet = LocalDateTime.now();
        }
        if (jobToUpdate.getDone() != null) {
            errors.add(new ValidationError("date", "validation.job.alreadyDone"));
        }

        if (jobToUpdate.getJobGroup().getBringedin().isAfter(dateToSet)) {
            errors.add(new ValidationError("date", "validation.job.beforeBringin"));
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
        jobToUpdate.setDone(dateToSet);

        if (jobCompleteData.getPrice() == null) {
            jobToUpdate.setFinalprice(jobToUpdate.getPricemin());
        } else {
            jobToUpdate.setFinalprice(jobCompleteData.getPrice());
        }
        Set<JobImage> afterImages = jobToUpdate.getAfterImages();

        jobToUpdate.setFinishnote(jobCompleteData.getNote());
        if (jobCompleteData.getImagesAfter() != null) {
            afterImages.addAll(imageService.attachImagesByIds(jobCompleteData.getImagesAfter(), jobToUpdate, ImageType.AFTER));
        }
        jobRepository.save(jobToUpdate);
    }



    /** {@inheritDoc} */
    @Override
    @Transactional
    public void pickedUpJob(Integer id, JobPickedUpDTO jobPickedUpData) {
        Job jobToUpdate = findJobById(id);
        List<ValidationError> errors = new LinkedList<>();
        
        LocalDateTime pickupDate = jobPickedUpData.getDate();
        if (pickupDate == null) {
            pickupDate = LocalDateTime.now();
        }
        if(pickupDate.isBefore(jobToUpdate.getDone())) {
            errors.add(new ValidationError("date", "validation.job.pickup.before.done"));
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
        jobToUpdate.setPickup(pickupDate);
        jobRepository.save(jobToUpdate);
    }

    private Job findJobById(Integer id) {
        return jobRepository.findById(id).orElseThrow(() -> new JobNotFoundException("Job with ID:" + id + " not found", "error.job.not.found"));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public Page<JobSearchJobDataDTO> searchForJob(JobSearchDto filter, Pageable pageable) {
        Specification<Job> spec = JobSearchSpec.withFilters(filter);
        Pageable mappedSortPageable = applySortMappings(pageable);
        Page<Job> jobs = jobRepository.findAll(spec, mappedSortPageable);
        return jobs.map(job -> {
            JobGroup jobGroup = job.getJobGroup();
            Person person = jobGroup.getPerson();
            return new JobSearchJobDataDTO(
                    person.getId(),
                    jobGroup.getId(),
                    job.getId(),
                    person.getName(),
                    job.getObjectname(),
                    job.getDescription(),
                    jobGroup.getBringedin(),
                    job.getDone(),
                    job.getPickup()
            );
        });
    }

    private Pageable applySortMappings(Pageable pageable) {
        if (pageable == null || pageable.getSort().isUnsorted()) {
            return pageable;
        }

        List<Sort.Order> mappedOrders = new LinkedList<>();
        for (Sort.Order order : pageable.getSort()) {
            String mappedProperty = mapSortProperty(order.getProperty());
            if (mappedProperty != null) {
                mappedOrders.add(order.withProperty(mappedProperty));
            }
        }

        if (mappedOrders.isEmpty()) {
            // If nothing matched, keep the original pageable to avoid silently dropping sorting
            return pageable;
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(mappedOrders));
    }

    private String mapSortProperty(String property) {
        if (property == null) {
            return null;
        }
        String key = property.toLowerCase();
        return switch (key) {
            case "personid", "person.id", "person" -> "jobGroup.person.id";
            case "jobgroupid", "jobgroup.id", "jobgroup" -> "jobGroup.id";
            case "jobid", "id" -> "id";
            case "personname", "person.name" -> "jobGroup.person.name";
            case "objectname", "object" -> "objectname";
            case "description" -> "description";
            case "bringin", "bringedin" -> "jobGroup.bringedin";
            case "done" -> "done";
            case "pickup" -> "pickup";
            default -> null;
        };
    }



    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public byte[] getSummary(Integer id) {
        return this.makeSummery(findJobById(id));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public Job getJobById(Integer id) {
        return findJobById(id);
    }
    private byte[] makeSummery(Job job) {
        byte[] output = null;
        try {
            // Create a ByteArrayOutputStream to hold the PDF data
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // Initialize the PDF document
            Document document = new Document(PageSize.A4, 36, 36, 10, 36); // Reduce top margin

            PdfWriter.getInstance(document, outputStream); // Write to the stream
            document.open();
            Font bigTitle = FontFactory.getFont(FontFactory.HELVETICA, 16f); // Bold, size 14
            Font dataFont = new Font(Font.HELVETICA, 12f); // Regular, size 12
            com.lowagie.text.Table table = new com.lowagie.text.Table(2);
            table.setSpacing(0f);
            table.setPadding(3f);
            table.setWidth(100f);
            table.setBorderWidth(0f);
            Cell titleCell = new Cell();
            titleCell.setColspan(2);
            Paragraph titleParagraph = new Paragraph("Ékrszerjavítás összegzés", bigTitle);
            titleCell.setVerticalAlignment(VerticalAlignment.CENTER);
            titleCell.setHorizontalAlignment(HorizontalAlignment.CENTER);
            titleCell.setBorderWidthBottom(1f);
            titleCell.addElement(titleParagraph);
            table.addCell(titleCell);
            Cell personTitleCell = new Cell();
            personTitleCell.setColspan(2);
            Paragraph personTitleParagraph = new Paragraph("Ügyfél adatai");
            personTitleCell.addElement(personTitleParagraph);
            personTitleCell.setVerticalAlignment(VerticalAlignment.CENTER);
            personTitleCell.setHorizontalAlignment(HorizontalAlignment.CENTER);
            personTitleCell.setBorderWidth(0f);
            table.addCell(personTitleCell);

            Cell personInfoCell = new Cell();
            personInfoCell.setColspan(2);
            personInfoCell.setColspan(2);
            Paragraph personInfoParagraph = new Paragraph("Név: " + job.getJobGroup().getPerson().getName() + "\nLakcím: "
                    + job.getJobGroup().getPerson().getAddress()
                    + (job.getJobGroup().getPerson().getPhone() != null ? ("\nTelefonszám: " + job.getJobGroup().getPerson().getPhone())
                            : ""),
                    dataFont);
            personInfoCell.addElement(personInfoParagraph);
            personInfoCell.setBorderWidthTop(1f);
            personInfoCell.setBorderWidthBottom(1f);
            table.addCell(personInfoCell);

            Cell jobTitleCell = new Cell();
            jobTitleCell.setColspan(2);
            Paragraph jobTitleParagraph = new Paragraph("Munka adatai");
            jobTitleCell.addElement(jobTitleParagraph);
            jobTitleCell.setVerticalAlignment(VerticalAlignment.CENTER);
            jobTitleCell.setHorizontalAlignment(HorizontalAlignment.CENTER);
            jobTitleCell.setBorderWidthBottom(1f);
            table.addCell(jobTitleCell);

            Cell jobInfoCell = new Cell();
            Paragraph jobInfoParagraph = new Paragraph(
                    "Tárgy: " + job.getObjectname() +
                            "\nAnyaga: " + job.getMaterial() +
                            "\nSúlya: " + job.getWeight() + "gr" +
                            "\nÁra:" + job.getPricemin() + (job.getPricemax() != null ? (" - " + job.getPricemax()) : "") + "Ft",
                    dataFont);
            jobInfoCell.addElement(jobInfoParagraph);
            jobInfoCell.setBorderWidthBottom(1f);
            jobInfoCell.setBorderWidthRight(1f);
            table.addCell(jobInfoCell);
            Cell descriptionCell = new Cell();
            Paragraph descriptionParagraph = new Paragraph("Kért munka:\n" + job.getDescription(), dataFont);
            descriptionCell.addElement(descriptionParagraph);
            descriptionCell.setBorderWidthBottom(1f);
            table.addCell(descriptionCell);
            
            com.lowagie.text.Table dateTable = new com.lowagie.text.Table(3);
            dateTable.setWidth(100f);
            dateTable.setPadding(3f);
            dateTable.setBorderWidth(0f);
            Cell uploadTitleCell = new Cell();
            Paragraph uploadTitleParagraph = new Paragraph("Behozatali dátum\n"+deserializeDate(job.getJobGroup().getBringedin()));
            uploadTitleCell.addElement(uploadTitleParagraph);
            uploadTitleCell.setBorderWidthRight(1f);
            uploadTitleCell.setHorizontalAlignment(HorizontalAlignment.CENTER);
            uploadTitleCell.setVerticalAlignment(VerticalAlignment.TOP);
            dateTable.addCell(uploadTitleCell);

            Cell finishTitleCell = new Cell();
            Paragraph finishTitleParagraph = new Paragraph("Elvégzési dátum\n"+deserializeDate(job.getDone()));
            finishTitleCell.addElement(finishTitleParagraph);
            finishTitleCell.setBorderWidthRight(1f);
            finishTitleCell.setHorizontalAlignment(HorizontalAlignment.CENTER);
            finishTitleCell.setVerticalAlignment(VerticalAlignment.CENTER);
            dateTable.addCell(finishTitleCell);

            Cell pickupTitleCell = new Cell();
            Paragraph pickupTitleParagraph = new Paragraph("Elviteli dátum\n"+deserializeDate(job.getPickup()));
            pickupTitleCell.addElement(pickupTitleParagraph);
            pickupTitleCell.setBorderWidth(0f);
            pickupTitleCell.setHorizontalAlignment(HorizontalAlignment.CENTER);
            pickupTitleCell.setVerticalAlignment(VerticalAlignment.CENTER);
            dateTable.addCell(pickupTitleCell);

            document.add(table);


            document.add(dateTable);

            Integer picsPerRow = 4;
            if (job.getBeforeImage().size() != 0) {

                com.lowagie.text.Table picTable = new com.lowagie.text.Table(picsPerRow);
                picTable.setWidth(100f);
                picTable.setPadding(3f);
                picTable.setBorderWidth(0f);
                Cell imageTitleCell = new Cell();
                imageTitleCell.setBorderWidth(0f);
                Paragraph imageTitleParagraph = new Paragraph("Elvégzés elötti képek");
                imageTitleCell.addElement(imageTitleParagraph);
                imageTitleCell.setColspan(picsPerRow);
                imageTitleCell.setBorderWidthTop(1f);
                imageTitleCell.setVerticalAlignment(VerticalAlignment.CENTER);
                imageTitleCell.setHorizontalAlignment(HorizontalAlignment.CENTER);
                picTable.addCell(imageTitleCell);
                for (JobImage image : job.getBeforeImage()) {

                    Cell imageCell = new Cell();
                    imageCell.setBorderWidth(0f);
                    Image img = Image.getInstance(handleImageExifData(this.imageService.getFullImageFile(image.getId()))); 
                    img.rotate();
                    imageCell.add(img);
                    picTable.addCell(imageCell);
                }
                document.add(picTable);
            }
            
            if (job.getAfterImages().size() != 0) {
                com.lowagie.text.Table picTable = new com.lowagie.text.Table(picsPerRow);
                picTable.setWidth(100f);
                picTable.setPadding(3f);
                picTable.setBorderWidth(0f);
                Cell imageTitleCell = new Cell();
                imageTitleCell.setBorderWidth(0f);
                Paragraph imageTitleParagraph = new Paragraph("Elvégzés utáni képek");
                imageTitleCell.addElement(imageTitleParagraph);
                imageTitleCell.setBorderWidthTop(1f);
                imageTitleCell.setColspan(picsPerRow);
                imageTitleCell.setVerticalAlignment(VerticalAlignment.CENTER);
                imageTitleCell.setHorizontalAlignment(HorizontalAlignment.CENTER);
                picTable.addCell(imageTitleCell);
                for (JobImage image : job.getAfterImages()) {
                    
                    Cell imageCell = new Cell();
                    imageCell.setBorderWidth(0f);
                    Image img = Image.getInstance(handleImageExifData(this.imageService.getFullImageFile(image.getId()))); 
                    img.rotate();
                    imageCell.add(img);
                    picTable.addCell(imageCell);
                }
                document.add(picTable);
            }
            //TODO: Handle image title placement if the images can't fit on the same page
            document.close();
            output = outputStream.toByteArray();

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return output;
    }

    private String deserializeDate(LocalDateTime date){
        if(date == null){
            return "Nincs megadva";
        }
        return date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd. HH:mm"));
    }

    private byte[] handleImageExifData(File image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Thumbnails.of(image)
            .scale(1.0)
            .useExifOrientation(true)
            .toOutputStream(baos);
        return baos.toByteArray();
    }
}
