package otvosuzlet.javitasnyilntarto.service;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import otvosuzlet.javitasnyilntarto.dto.SystemSettingDto;
import otvosuzlet.javitasnyilntarto.dto.SystemSettingSearchRequest;
import otvosuzlet.javitasnyilntarto.exceptions.SystemSettingNotFoundException;
import otvosuzlet.javitasnyilntarto.model.SystemSetting;
import otvosuzlet.javitasnyilntarto.repository.SystemSettingRepository;

@ExtendWith(MockitoExtension.class)
public class SystemSettingServiceImplTest {

    @Mock
    private SystemSettingRepository systemSettingRepository;

    @Mock
    private ConversionService conversionService;

    @InjectMocks
    private SystemSettingServiceImpl systemSettingService;

    @Test
    void getStringShouldReturnNormalizedValue() throws Exception {
        SystemSetting setting = new SystemSetting("test.key", "test.description", "test \\n value");
        Mockito.when(systemSettingRepository.findById("test.key")).thenReturn(Optional.of(setting));
        String value = systemSettingService.getValue("test.key");
        assert(value.equals("test \n value"));
    }
    
    @Test
    void getStringShouldThrowExceptionWhenSettingNotFound() throws Exception {
        Mockito.when(systemSettingRepository.findById("missing.key")).thenReturn(Optional.empty());
        Assertions.assertThrows(SystemSettingNotFoundException.class, () -> systemSettingService.getValue("missing.key"));
    }

    @Test
    void updateValueShouldThrowExceptionWhenSettingNotFound() throws Exception {
        Mockito.when(systemSettingRepository.findById("missing.key")).thenReturn(Optional.empty());
        Assertions.assertThrows(SystemSettingNotFoundException.class, () -> systemSettingService.updateValue("missing.key", "new value"));
    }

    @Test
    void updateValueShouldUpdateAndSaveSetting() throws Exception {
        SystemSetting setting = new SystemSetting("test.key", "test.description", "old value");
        Mockito.when(systemSettingRepository.findById("test.key")).thenReturn(Optional.of(setting));
        systemSettingService.updateValue("test.key", "new value");
        Assertions.assertEquals("new value", setting.getValue());
        Mockito.verify(systemSettingRepository).save(setting);
    }

    @Test
    void searchByKeyShouldUseSpecificationForNonBlankTerm() {
        Pageable pageable = PageRequest.of(0, 10);
        SystemSetting setting = new SystemSetting("receipt.title", "desc.key", "Some Title");
        SystemSettingDto dto = new SystemSettingDto();
        dto.setKey("receipt.title");
        SystemSettingSearchRequest search = new SystemSettingSearchRequest();
        search.setKey("receipt");

        Page<SystemSetting> repoPage = new PageImpl<>(java.util.List.of(setting), pageable, 1);
        Mockito.when(systemSettingRepository.findAll(Mockito.<Specification<SystemSetting>>any(), Mockito.eq(pageable))).thenReturn(repoPage);
        Mockito.when(conversionService.convert(setting, SystemSettingDto.class)).thenReturn(dto);

        Page<SystemSettingDto> result = systemSettingService.searchByKey(search, pageable);

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(dto.getKey(), result.getContent().get(0).getKey());
        Mockito.verify(systemSettingRepository, Mockito.times(1)).findAll(Mockito.<Specification<SystemSetting>>any(), Mockito.eq(pageable));
    }

    @Test
    void searchByKeyShouldUseSpecificationForBlankTerm() {
        Pageable pageable = PageRequest.of(0, 10);
        SystemSetting setting = new SystemSetting("receipt.prefix", "desc.key", "REC");
        SystemSettingDto dto = new SystemSettingDto();
        dto.setKey("receipt.prefix");
        SystemSettingSearchRequest search = new SystemSettingSearchRequest();
        search.setKey("   ");

        Page<SystemSetting> repoPage = new PageImpl<>(java.util.List.of(setting), pageable, 1);
        Mockito.when(systemSettingRepository.findAll(Mockito.<Specification<SystemSetting>>any(), Mockito.eq(pageable))).thenReturn(repoPage);
        Mockito.when(conversionService.convert(setting, SystemSettingDto.class)).thenReturn(dto);

        Page<SystemSettingDto> result = systemSettingService.searchByKey(search, pageable);

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(dto.getKey(), result.getContent().get(0).getKey());
        Mockito.verify(systemSettingRepository, Mockito.times(1)).findAll(Mockito.<Specification<SystemSetting>>any(), Mockito.eq(pageable));
    }
}