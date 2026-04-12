package otvosuzlet.javitasnyilntarto.converter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import otvosuzlet.javitasnyilntarto.dto.SystemSettingDto;
import otvosuzlet.javitasnyilntarto.model.SystemSetting;

public class SystemSettingToSystemSettingDtoConverterTest {

    @Test
    public void convertShouldMapAllFields() {
        SystemSettingToSystemSettingDtoConverter converter = new SystemSettingToSystemSettingDtoConverter();
        SystemSetting setting = new SystemSetting();
        setting.setKey("jobs.archive.days");
        setting.setDescriptionKey("system.setting.jobs.archive.days");
        setting.setValue("90");

        SystemSettingDto result = converter.convert(setting);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(setting.getKey(), result.getKey());
        Assertions.assertEquals(setting.getDescriptionKey(), result.getDescriptionKey());
        Assertions.assertEquals(setting.getValue(), result.getValue());
    }
}
