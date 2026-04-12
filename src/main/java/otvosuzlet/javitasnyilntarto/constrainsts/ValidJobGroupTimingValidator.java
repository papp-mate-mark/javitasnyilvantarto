package otvosuzlet.javitasnyilntarto.constrainsts;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import otvosuzlet.javitasnyilntarto.dto.JobDto;
import otvosuzlet.javitasnyilntarto.dto.JobGroupDto;

import java.time.LocalDateTime;
import java.util.List;

public class ValidJobGroupTimingValidator implements ConstraintValidator<ValidJobGroupTiming, JobGroupDto> {

    @Override
    public boolean isValid(JobGroupDto jobGroup, ConstraintValidatorContext context) {
        if (jobGroup == null) {
            return true;
        }

        LocalDateTime bringin = jobGroup.getBringin();
        LocalDateTime deadline = jobGroup.getDeadline();
        List<JobDto> jobs = jobGroup.getJobs();
        boolean isValid = true;

        if (jobs == null) {
            return true;
        }

        for (int i = 0; i < jobs.size(); i++) {
            JobDto job = jobs.get(i);
            LocalDateTime finish = job.getFinishTime();
            LocalDateTime pickup = job.getPickedUpTime();
            String finishNote = job.getFinishnote();
            Integer finalPrice = job.getFinalPrice();
            List<Integer> imagesAfter = job.getImagesAfter();
            Integer minPrice = job.getPricemin();
            Integer maxPrice = job.getPricemax();


            if (finish != null) {
                // Bringin must exist and be before finishTime
                if (bringin == null) {
                        addViolation(context, "validation.bringin.required", "bringin");
                    isValid = false;
                } else if (finish.isBefore(bringin)) {
                        addJobFieldViolation(context, "validation.finishTime.afterBringin", "finishTime", i);
                    isValid = false;
                }

                // pickedUpTime must be after finishTime if present
                if (pickup != null && pickup.isBefore(finish)) {
                        addJobFieldViolation(context, "validation.pickedUpTime.afterFinishTime", "pickedUpTime", i);
                    isValid = false;
                }
            } else {
                // If finishTime is null, but pickedUpTime is set, finishTime is required
                if (pickup != null) {
                        addJobFieldViolation(context, "validation.finishTime.requiredIfFinishTimeIsSet", "finishTime", i);
                    isValid = false;
                }
            }
            if(finalPrice !=null && finish == null)
            {
                addJobFieldViolation(context, "validation.finishTime.requiredIfFinalPriceIsSet", "finishTime", i);
                isValid = false;
            }
            if(finishNote !=null && finish == null)
            {
                addJobFieldViolation(context, "validation.finishTime.requiredIfFinishNoteIsSet", "finishTime", i);
                isValid = false;
            }
            if(finish == null && (imagesAfter != null && imagesAfter.size()!=0)){
                addJobFieldViolation(context, "validation.finishTime.requiredIfImagesAfterIsSet", "finishTime", i);
                isValid = false;
            }
            if(minPrice != null && maxPrice != null && maxPrice < minPrice)
            {
                addJobFieldViolation(context, "validation.pricemax.greaterThanPricemin", "pricemax", i);   
                isValid = false;
            }

        }
        if(deadline != null && ((bringin == null && deadline.isBefore(LocalDateTime.now())) || (bringin != null && deadline.isBefore(bringin))))
        {
            addViolation(context, "validation.deadline.afterBringin", "deadline");
            isValid = false;
        }

        return isValid;
    }
    
    private void addViolation(ConstraintValidatorContext context, String message, String field) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(field)
                .addConstraintViolation();
    }
    
    private void addJobFieldViolation(ConstraintValidatorContext context, String message, String field, int index) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode("jobs")
                .addPropertyNode(field)
                .inIterable().atIndex(index)
                .addBeanNode()
                .addConstraintViolation();
    }
}
