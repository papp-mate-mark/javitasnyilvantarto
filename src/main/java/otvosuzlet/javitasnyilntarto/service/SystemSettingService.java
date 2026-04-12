package otvosuzlet.javitasnyilntarto.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import otvosuzlet.javitasnyilntarto.dto.SystemSettingDto;
import otvosuzlet.javitasnyilntarto.dto.SystemSettingSearchRequest;

public interface SystemSettingService {
    
    /**
     * Retrieves the value of a system setting associated with the given key.
     * 
     * @param key The key of the system setting to retrieve.
     * @return The value of the system setting, or null if not found.
     */
    public String getValue(String key);

    /**
     * Fetches all system settings with pagination.
     * 
     * @param pageable The pagination information.
     * @return A paginated list of system setting DTOs.
     */
    public Page<SystemSettingDto> fetchAll(Pageable pageable);

    /**
     * Searches for system settings by key using the provided search criteria.
     * 
     * @param search The search request containing the key or partial key to search for.
     * @param pageable The pagination information.
     * @return A paginated list of system setting DTOs matching the search criteria.
     */
    public Page<SystemSettingDto> searchByKey(SystemSettingSearchRequest search, Pageable pageable);

    /**
     * Updates the value of an existing system setting.
     * 
     * @param key The key of the system setting to update.
     * @param newValue The new value to set for the system setting.
     */
    public void updateValue(String key, String newValue);
}
