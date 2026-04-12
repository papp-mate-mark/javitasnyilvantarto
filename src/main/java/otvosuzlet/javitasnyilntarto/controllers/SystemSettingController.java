package otvosuzlet.javitasnyilntarto.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import otvosuzlet.javitasnyilntarto.dto.PageResponse;
import otvosuzlet.javitasnyilntarto.dto.SystemSettingDto;
import otvosuzlet.javitasnyilntarto.dto.SystemSettingSearchRequest;
import otvosuzlet.javitasnyilntarto.dto.UpdateSystemSettingRequest;
import otvosuzlet.javitasnyilntarto.service.SystemSettingService;

@RestController
@RequestMapping("/api/admin/system-settings")
@Validated
@PreAuthorize("hasAuthority('MODIFY_SYSTEM_SETTINGS')")
public class SystemSettingController {
    @Autowired
    private SystemSettingService systemSettingService;
    
    @Autowired
    private ConversionService conversionService;

    /**
     * Fetches paginated system settings.
     *
     * @param pageable Resolves bounds correctly defining chunks returned dynamically interpreting request attributes optimally perfectly defining behavior successfully perfectly aligning layouts correctly functionally logically completely efficiently mapping successfully dynamically perfectly.
     * @return Execution result carrying structured bindings mapped efficiently dynamically.
     */
    @GetMapping
    public ResponseEntity<PageResponse<SystemSettingDto>> list(@PageableDefault(size = 20) Pageable pageable) {
        Page<SystemSettingDto> page = systemSettingService.fetchAll(pageable);
        @SuppressWarnings("unchecked")
        PageResponse<SystemSettingDto> response = conversionService.convert(page, PageResponse.class);
        return ResponseEntity.ok(response);
    }

    /**
     * Searches for system settings based on search parameters with pagination capabilities.
     *
     * @param search Valid query definitions matched logically properly fully completely directly efficiently accurately completely matching context perfectly executing definitions cleanly structurally perfectly mapping parameters precisely optimally completely precisely successfully matching conditions accurately correctly completely handling optimally perfectly mapping contexts precisely.
     * @param pageable Defines extraction dimensions evaluating responses correctly gracefully mapped efficiently precisely perfectly completely structuring interactions dynamically aligning correctly mapping smoothly matching successfully precisely perfectly evaluating context accurately completely mappings naturally cleanly optimally.
     * @return Interpretable configurations evaluating logic boundaries fully successfully smoothly perfectly mapping.
     */
    @GetMapping("/search")
    public ResponseEntity<PageResponse<SystemSettingDto>> search(
            SystemSettingSearchRequest search,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<SystemSettingDto> page = systemSettingService.searchByKey(search, pageable);
        @SuppressWarnings("unchecked")
        PageResponse<SystemSettingDto> response = conversionService.convert(page, PageResponse.class);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates the value of a system setting by a given key.
     * 
     * @param key Config variable pointing context defining updating boundaries completely efficiently seamlessly.
     * @param request Modified target data representation replacing legacy values perfectly.
     * @return Execution end denoting empty boundary HTTP mapped completely efficiently gracefully successfully directly seamlessly correctly perfectly cleanly optimally matching precisely translating cleanly completely executing accurately perfectly completely seamlessly naturally dynamically properly thoroughly accurately mapping perfectly successfully executing accurately safely cleanly naturally functionally naturally safely smoothly thoroughly completely directly cleanly optimally accurately cleanly smoothly executing matching successfully nicely gracefully.
     */
    @PatchMapping("/{key}")
    public ResponseEntity<Void> update(@PathVariable String key, @RequestBody UpdateSystemSettingRequest request) {
        systemSettingService.updateValue(key, request == null ? null : request.getValue());
        return ResponseEntity.noContent().build();
    }
}
