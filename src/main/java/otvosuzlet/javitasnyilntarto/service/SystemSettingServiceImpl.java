package otvosuzlet.javitasnyilntarto.service;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import otvosuzlet.javitasnyilntarto.dto.SystemSettingDto;
import otvosuzlet.javitasnyilntarto.dto.SystemSettingSearchRequest;
import otvosuzlet.javitasnyilntarto.exceptions.SystemSettingNotFoundException;
import otvosuzlet.javitasnyilntarto.repository.SystemSettingRepository;
import otvosuzlet.javitasnyilntarto.model.SystemSetting;
import otvosuzlet.javitasnyilntarto.specification.SystemSettingSearchSpec;

@Service
public class SystemSettingServiceImpl implements SystemSettingService {

    @Autowired
    private SystemSettingRepository systemSettingRepository;

    @Autowired
    private ConversionService conversionService;

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public String getValue(String key) {
        Optional<SystemSetting> setting = systemSettingRepository.findById(key);
        String value = setting.map(SystemSetting::getValue).orElseThrow(()-> new SystemSettingNotFoundException("System setting not found", "error.system.setting.not.found"));
        return normalize(value);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public Page<SystemSettingDto> fetchAll(Pageable pageable) {
        return systemSettingRepository.findAll(pageable).map(setting -> conversionService.convert(setting, SystemSettingDto.class));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public Page<SystemSettingDto> searchByKey(SystemSettingSearchRequest search, Pageable pageable) {
        return systemSettingRepository.findAll(SystemSettingSearchSpec.withFilters(search), pageable)
                .map(setting -> conversionService.convert(setting, SystemSettingDto.class));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void updateValue(String key, String newValue) {
        SystemSetting setting = systemSettingRepository.findById(key)
                .orElseThrow(() -> new SystemSettingNotFoundException("System setting not found.", "error.system.setting.not.found"));
        setting.setValue(newValue);
        systemSettingRepository.save(setting);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        // Allow storing multi-line text as a single-line DB value containing literal "\n".
        // This keeps admin editing simple while rendering still uses real line breaks.
        return value.replace("\\n", "\n");
    }

  
}
