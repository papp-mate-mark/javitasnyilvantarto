package otvosuzlet.javitasnyilntarto.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import otvosuzlet.javitasnyilntarto.dto.SystemSettingDto;
import otvosuzlet.javitasnyilntarto.model.SystemSetting;

@Component
public class SystemSettingToSystemSettingDtoConverter implements Converter<SystemSetting, SystemSettingDto> {
    @Override
    public SystemSettingDto convert(SystemSetting source) {
        SystemSettingDto dto = new SystemSettingDto();
        dto.setKey(source.getKey());
        dto.setDescriptionKey(source.getDescriptionKey());
        dto.setValue(source.getValue());

        return dto;
    }

}
