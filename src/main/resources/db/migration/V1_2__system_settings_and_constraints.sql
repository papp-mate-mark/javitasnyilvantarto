-- Add basic system settings storage for configurable long texts (e.g., receipt strings)
CREATE TABLE system_settings (
    setting_key VARCHAR(255) PRIMARY KEY,
    description_key VARCHAR(255),
    setting_value TEXT
);


INSERT INTO system_settings (setting_key, description_key, setting_value) VALUES
    ('receipt.title', 'receipt.title.description', 'Átvételi elismervény\nÉkszerjavításról'),
    ('receipt.store_data', 'receipt.store_data.description', 'Példa üzlet leírás'),
    ('receipt.store_contact', 'receipt.store_contact.description', 'pelda@email.hu \n+36 20 123 4567\nValami utca 1, 1234 Város'),
    ('receipt.note', 'receipt.note.description', 'Valami megjegyzés a nyugtán');

