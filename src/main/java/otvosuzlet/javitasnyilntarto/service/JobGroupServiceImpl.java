package otvosuzlet.javitasnyilntarto.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lowagie.text.Cell;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.alignment.HorizontalAlignment;
import com.lowagie.text.alignment.VerticalAlignment;
import com.lowagie.text.pdf.PdfWriter;

import otvosuzlet.javitasnyilntarto.dto.ActiveJobsRequestDTO;
import otvosuzlet.javitasnyilntarto.dto.JobDto;
import otvosuzlet.javitasnyilntarto.dto.JobGroupDto;
import otvosuzlet.javitasnyilntarto.dto.JobGroupUploadResponse;
import otvosuzlet.javitasnyilntarto.dto.JobPickedUpDTO;
import otvosuzlet.javitasnyilntarto.enums.ImageType;
import otvosuzlet.javitasnyilntarto.exceptions.PersonNotFoundException;
import otvosuzlet.javitasnyilntarto.exceptions.RuntimeExceptionWithCode;
import otvosuzlet.javitasnyilntarto.exceptions.ValidationException;
import otvosuzlet.javitasnyilntarto.model.Job;
import otvosuzlet.javitasnyilntarto.model.JobGroup;
import otvosuzlet.javitasnyilntarto.model.JobImage;
import otvosuzlet.javitasnyilntarto.model.Person;
import otvosuzlet.javitasnyilntarto.projections.JobFullInfoProjection;
import otvosuzlet.javitasnyilntarto.projections.JobGroupFullInfoProjection;
import otvosuzlet.javitasnyilntarto.projections.PersonFullInfoProjection;
import otvosuzlet.javitasnyilntarto.repository.JobGroupRepository;
import otvosuzlet.javitasnyilntarto.repository.JobRepository;
import otvosuzlet.javitasnyilntarto.repository.PersonRepository;

@Service
public class JobGroupServiceImpl implements JobGroupService {
    private static final Logger logger = LoggerFactory.getLogger(JobGroupServiceImpl.class);

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobGroupRepository jobGroupRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired 
    private UserService userService;

    @Autowired
    private JobImageService imageService;

    @Autowired
    private SystemSettingService systemSettingService;

    @Transactional(readOnly = true)
    private Person findPersonById(Integer personId) {
        return personRepository.findById(personId)
                .orElseThrow(() -> new PersonNotFoundException("Person with ID:" + personId + " not found", "error.person.not.found"));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public JobGroupUploadResponse addJobGroupToPerson(JobGroupDto uploadDto, Integer personId, String uploaderUsername) {
        Person person = findPersonById(personId);
        JobGroup jobGroup = new JobGroup();
        jobGroup.setPerson(person);

        jobGroup.setBringedin(uploadDto.getBringin() == null ? LocalDateTime.now() : uploadDto.getBringin());
        jobGroup.setDeadline(uploadDto.getDeadline());

        jobGroup.setUser(userService.findByUsername(uploaderUsername));

        Set<Job> jobs = new HashSet<>();
        for (JobDto jobDto : uploadDto.getJobs()) {
            Job job = new Job();
            job.setObjectname(jobDto.getObjectname());
            job.setDescription(jobDto.getDescription());
            job.setMaterial(jobDto.getMaterial());
            job.setPricemin(jobDto.getPricemin());
            job.setPricemax(jobDto.getPricemax());
            job.setWeight(jobDto.getWeight());
            job.setJobGroup(jobGroup);
            job.setDone(jobDto.getFinishTime());
            job.setPickup(jobDto.getPickedUpTime());
            job.setFinalprice(jobDto.getFinalPrice());
            job.setUploadnote(jobDto.getUploadnote());
            job.setFinishnote(jobDto.getFinishnote());
            job.setBeforeImage(new HashSet<>());
            job.setAfterImages(new HashSet<>());
            Set<JobImage> beforeImages = imageService.attachImagesByIds(jobDto.getImagesBefore(), job, ImageType.BEFORE);
            job.setBeforeImage(beforeImages);

            Set<JobImage> afterImages = imageService.attachImagesByIds(jobDto.getImagesAfter(), job, ImageType.AFTER);
            job.setAfterImages(afterImages);
            jobs.add(job);
        }

        jobGroup.setJobs(jobs);


        JobGroup savedJobGroup = jobGroupRepository.save(jobGroup);
        JobGroupUploadResponse response = new JobGroupUploadResponse(savedJobGroup.getId());
        return response;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public ActiveJobsRequestDTO getActiveJobsGroups() {
        Set<PersonFullInfoProjection> activePersonReq = personRepository.findPersonsWithActiveJobsProjection();
        Set<ActiveJobsRequestDTO.JobGroup> jobGroups = new HashSet<>();

        for (PersonFullInfoProjection person : activePersonReq) {
            for (JobGroupFullInfoProjection jobGroup : person.getJobGroups()) {
                Set<ActiveJobsRequestDTO.JobGroup.DoneJob> doneJobs = new HashSet<>();
                Set<ActiveJobsRequestDTO.JobGroup.InProgressJob> inProgressJobs = new HashSet<>();
                for (JobFullInfoProjection job : jobGroup.getJobs()) {
                    if (job.getDone() == null) {
                        inProgressJobs.add(new ActiveJobsRequestDTO.JobGroup.InProgressJob(job.getId(),
                                job.getDescription(), job.getObjectname(), job.getPricemin(), job.getPricemax()));
                    } else {
                        doneJobs.add(new ActiveJobsRequestDTO.JobGroup.DoneJob(job.getId(), job.getDescription(),
                                job.getObjectname(), job.getFinalprice(), job.getDone()));
                    }
                }
                ActiveJobsRequestDTO.JobGroup newGroup = new ActiveJobsRequestDTO.JobGroup(jobGroup.getId(),
                        person.getId(), person.getName(), jobGroup.getBringedin(), jobGroup.getDeadline(),
                        inProgressJobs, doneJobs);
                jobGroups.add(newGroup);
            }
        }
        ActiveJobsRequestDTO result = new ActiveJobsRequestDTO();
        result.setGroups(jobGroups);
        return result;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void setDoneJobsToPickedUp(Integer id, JobPickedUpDTO jobPickedUpData) {
        JobGroup group = findJobGroupById(id);
        LocalDateTime pickupDate = jobPickedUpData.getDate();
        if(pickupDate == null){
            pickupDate = LocalDateTime.now();
        }
        if(group.getBringedin().isAfter(pickupDate)){
            throw new ValidationException("date", "validation.job.pickedup.before.bringedin");
        }
        for (Job job : group.getJobs()) {
            if(job.getDone() != null && job.getDone().isAfter(pickupDate)){
                throw new ValidationException("date", "validation.job.pickedup.before.done");
            }
        }

        for (Job job : group.getJobs()) {
            if (job.getPickup() == null && job.getDone() != null) {
                if (jobPickedUpData.getDate() == null) {
                    job.setPickup(LocalDateTime.now());
                } else {
                    job.setPickup(jobPickedUpData.getDate());
                }
                jobRepository.save(job);
            }
        }
    }
    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public byte[] getReceipt(Integer id){
        JobGroup jobGroup = findJobGroupById(id);
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, outputStream);
            document.open();
            Font bigTitle = FontFactory.getFont(FontFactory.HELVETICA, 14f);
            Font dataFont = new Font(Font.HELVETICA, 9f);
            Integer sumMin = 0;
            Integer sumMax = 0;
            for (Job job : jobGroup.getJobs()) {
                sumMin += job.getPricemin();
                sumMax += job.getPricemax() != null ? job.getPricemax() : job.getPricemin();
            }
            List<Job> alljobs = new ArrayList<>(jobGroup.getJobs());
            List<Job> remainingElements = new ArrayList<>(alljobs);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy. MM. dd. HH:mm");
            do {
                com.lowagie.text.Table topTable = new com.lowagie.text.Table(5);
                topTable.setWidths(new float[]{15f, 27.5f, 5f, 15f, 27.5f});
                topTable.setWidth(90f);
                topTable.setBorderWidth(0f);
                topTable.setBorderColor(Color.BLUE);
                topTable.setPadding(3f);
                Cell dateCell = new Cell();
                Paragraph dateParagraph = new Paragraph(
                        "Behozatali dátum:\n" + jobGroup.getBringedin().format(formatter) + "\nVállási határidő:\n"
                                + jobGroup.getDeadline().format(formatter),
                        dataFont);
                dateCell.add(dateParagraph);
                dateCell.setBorderWidthTop(0f);
                dateCell.setBorderWidthLeft(0f);

                Cell titleCell = new Cell();
            String receiptTitle = systemSettingService.getValue("receipt.title");
        Paragraph titleParagraph = new Paragraph(receiptTitle, bigTitle);
                titleCell.setVerticalAlignment(VerticalAlignment.CENTER);
                titleCell.setHorizontalAlignment(HorizontalAlignment.CENTER);
                titleCell.add(titleParagraph);
                titleCell.setBorderWidthTop(0f);
                titleCell.setBorderWidthRight(0f);
                titleCell.setBorderWidthLeft(1f);
                Cell emptyCell = new Cell();
                emptyCell.setBorderWidth(0f);

                topTable.addCell(dateCell);
                topTable.addCell(titleCell);
                topTable.addCell(emptyCell);
                topTable.addCell(dateCell);
                topTable.addCell(titleCell);

                com.lowagie.text.Table restTable = new com.lowagie.text.Table(5);
                restTable.setWidths(new float[]{23.75f, 23.75f, 5f, 23.75f, 23.75f});
                restTable.setBorderWidth(0f);
                restTable.setWidth(90f);
                restTable.setPadding(3f);
                Cell personInfoCell = new Cell();
                personInfoCell.setColspan(2);
                Paragraph personInfoParagraph = new Paragraph(
                        "Név: " + jobGroup.getPerson().getName() + "\nLakcím: " + jobGroup.getPerson().getAddress()
                                + (jobGroup.getPerson().getPhone() != null
                                        ? ("\nTelefonszám: " + jobGroup.getPerson().getPhone())
                                        : ""),
                        dataFont);
                personInfoCell.addElement(personInfoParagraph);
                personInfoCell.setBorderWidthRight(0f);
                personInfoCell.setBorderWidthTop(1f);
                personInfoCell.setBorderWidthBottom(1f);
                restTable.addCell(personInfoCell);
                restTable.addCell(emptyCell);
                restTable.addCell(personInfoCell);
                for (Job job : remainingElements) {
                    Cell jobInfoCell = new Cell();
                    Paragraph jobInfoParagraph = new Paragraph(
                            "Tárgy: " + job.getObjectname() +
                                    "\nAnyaga: " + job.getMaterial() +
                                    "\nSúlya: " + job.getWeight().toString().replace('.', ',') + "gr" +
                                    "\nÁra: " + job.getPricemin()
                                    + (job.getPricemax() != null ? (" - " + job.getPricemax()) : "") + "Ft",
                            dataFont);
                    jobInfoCell.addElement(jobInfoParagraph);
                    jobInfoCell.setBorderWidthBottom(1f);
                    jobInfoCell.setBorderWidthRight(1f);
                    Cell descriptionCell = new Cell();
                    Paragraph descriptionParagraph = new Paragraph("Kért munka:\n" + job.getDescription(), dataFont);
                    descriptionCell.addElement(descriptionParagraph);
                    descriptionCell.setBorderWidthBottom(1f);
                    restTable.addCell(jobInfoCell);
                    restTable.addCell(descriptionCell);
                    restTable.addCell(emptyCell);
                    restTable.addCell(jobInfoCell);
                    restTable.addCell(descriptionCell);
                }

                Cell finalPriceCell = new Cell();
                Paragraph finalPriceParagraph = new Paragraph(
                        "Munkák száma: " + jobGroup.getJobs().size() + " db. Ár összesen: " + sumMin
                                + (sumMin.equals(sumMax) ? "" : (" - " + sumMax)) + "Ft",
                        dataFont);
                finalPriceCell.addElement(finalPriceParagraph);
                finalPriceCell.setBorderWidthBottom(1f);
                finalPriceCell.setColspan(2);
                restTable.addCell(finalPriceCell);
                restTable.addCell(emptyCell);
                restTable.addCell(finalPriceCell);

                Cell noteCell = new Cell();
                String receiptNote = systemSettingService.getValue("receipt.note");
                Paragraph noteParagraph = new Paragraph(receiptNote, dataFont);
                noteCell.addElement(noteParagraph);
                noteCell.setBorderWidthBottom(1f);
                noteCell.setColspan(2);
                restTable.addCell(noteCell);
                restTable.addCell(emptyCell);
                restTable.addCell(noteCell);

                Cell storeDataCell = new Cell();
                String storeDataText = systemSettingService.getValue("receipt.store_data");
                Paragraph storeDataParagraph = new Paragraph(storeDataText, dataFont);
                storeDataCell.setVerticalAlignment(VerticalAlignment.CENTER);
                storeDataCell.setHorizontalAlignment(HorizontalAlignment.CENTER);
                storeDataCell.setBorderWidthBottom(1f);
                storeDataCell.setBorderWidthRight(1f);
                storeDataCell.addElement(storeDataParagraph);

                Cell storeContactInfoCell = new Cell();
                String storeContactText = systemSettingService.getValue("receipt.store_contact");
                Paragraph storeContactInfoParagraph = new Paragraph(storeContactText, dataFont);
                storeContactInfoCell.setVerticalAlignment(VerticalAlignment.CENTER);
                storeContactInfoCell.setHorizontalAlignment(HorizontalAlignment.CENTER);
                storeContactInfoCell.setBorderWidthBottom(1f);
                storeContactInfoCell.addElement(storeContactInfoParagraph);
                restTable.addCell(storeDataCell);
                restTable.addCell(storeContactInfoCell);
                restTable.addCell(emptyCell);
                restTable.addCell(storeDataCell);
                restTable.addCell(storeContactInfoCell);

                Cell customerSignPlaceCell = new Cell();
                Paragraph customerSignParagraph = new Paragraph("\n___________________\nMegrendelő aláírása", dataFont);
                customerSignPlaceCell.addElement(customerSignParagraph);
                customerSignPlaceCell.setVerticalAlignment(VerticalAlignment.CENTER);
                customerSignPlaceCell.setHorizontalAlignment(HorizontalAlignment.CENTER);
                customerSignPlaceCell.setBorderWidth(0f);

                Cell storeSignPlaceCell = new Cell();
                Paragraph storeSignPlaceParagraph = new Paragraph("\n___________________\nÜgyintéző aláírása", dataFont);
                storeSignPlaceCell.addElement(storeSignPlaceParagraph);
                storeSignPlaceCell.setVerticalAlignment(VerticalAlignment.CENTER);
                storeSignPlaceCell.setHorizontalAlignment(HorizontalAlignment.CENTER);
                storeSignPlaceCell.setBorderWidth(0f);
                restTable.addCell(customerSignPlaceCell);
                restTable.addCell(storeSignPlaceCell);
                restTable.addCell(emptyCell);
                restTable.addCell(customerSignPlaceCell);
                restTable.addCell(storeSignPlaceCell);

                if (remainingElements.size() == 1 || restTable.getDimension().getHeight() < 8) {
                    for (Job item : remainingElements) {
                        alljobs.remove(item);
                    }
                    remainingElements.clear();
                    remainingElements = new ArrayList<>(alljobs);

                    document.add(topTable);
                    document.add(restTable);
                    document.newPage();

                } else {
                    if (!remainingElements.isEmpty()) {
                        remainingElements.remove(remainingElements.size() - 1);
                    }
                }

            } while (alljobs.size() > 0);

            document.close();
            byte[] pdfBytes = outputStream.toByteArray();

            System.out.println("PDF saved to byte[] (length: " + pdfBytes.length + " bytes)");
            return pdfBytes;
        } catch (Exception e) {
            logger.error("Failed to generate receipt for job group id {}", id, e);
            if (e instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new RuntimeExceptionWithCode(
                "Failed to generate receipt. Please reload the site and contact the maintainer.",
                "error.receipt.generation.failed",
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    private JobGroup findJobGroupById(Integer id) {
        return jobGroupRepository.findById(id).orElseThrow(() -> new RuntimeException("Nincs ilyen munka"));
    }

    

}
