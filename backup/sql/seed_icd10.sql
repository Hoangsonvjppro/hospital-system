-- ============================================================
-- SEED: Danh mục ICD-10 phổ biến tại phòng mạch tư Việt Nam
-- ============================================================

USE clinic_management;

INSERT IGNORE INTO Icd10Code (code, name_vi, name_en, category) VALUES
-- Bệnh nhiễm trùng
('A09',   'Tiêu chảy và viêm dạ dày ruột nhiễm trùng',        'Infectious gastroenteritis and colitis',     'Bệnh nhiễm trùng'),
('A15',   'Lao hô hấp',                                        'Respiratory tuberculosis',                   'Bệnh nhiễm trùng'),
('B34.9', 'Nhiễm virus không xác định',                         'Viral infection, unspecified',               'Bệnh nhiễm trùng'),

-- Bệnh nội tiết, dinh dưỡng, chuyển hóa
('E10',   'Đái tháo đường type 1',                              'Type 1 diabetes mellitus',                   'Nội tiết - Chuyển hóa'),
('E11',   'Đái tháo đường type 2',                              'Type 2 diabetes mellitus',                   'Nội tiết - Chuyển hóa'),
('E11.9', 'Đái tháo đường type 2 không biến chứng',            'Type 2 diabetes mellitus without complications', 'Nội tiết - Chuyển hóa'),
('E78.0', 'Tăng cholesterol máu đơn thuần',                     'Pure hypercholesterolaemia',                 'Nội tiết - Chuyển hóa'),
('E78.5', 'Rối loạn lipid máu không xác định',                  'Dyslipidaemia, unspecified',                 'Nội tiết - Chuyển hóa'),

-- Rối loạn tâm thần
('F32',   'Giai đoạn trầm cảm',                                'Depressive episode',                         'Tâm thần'),
('F41.0', 'Rối loạn hoảng sợ',                                  'Panic disorder',                             'Tâm thần'),
('F41.1', 'Rối loạn lo âu lan tỏa',                             'Generalized anxiety disorder',               'Tâm thần'),
('G43.9', 'Đau nửa đầu (migraine) không xác định',             'Migraine, unspecified',                      'Thần kinh'),
('G47.0', 'Mất ngủ',                                            'Insomnia',                                   'Thần kinh'),

-- Bệnh mắt
('H10.9', 'Viêm kết mạc không xác định',                       'Conjunctivitis, unspecified',                'Mắt'),
('H66.9', 'Viêm tai giữa không xác định',                      'Otitis media, unspecified',                  'Tai'),

-- Bệnh tuần hoàn
('I10',   'Tăng huyết áp nguyên phát (vô căn)',                'Essential (primary) hypertension',            'Tim mạch'),
('I11.9', 'Bệnh tim do tăng huyết áp không suy tim',           'Hypertensive heart disease without heart failure', 'Tim mạch'),
('I20.9', 'Đau thắt ngực không xác định',                      'Angina pectoris, unspecified',               'Tim mạch'),
('I25.1', 'Bệnh tim thiếu máu cục bộ mãn tính',               'Atherosclerotic heart disease',               'Tim mạch'),
('I50.9', 'Suy tim không xác định',                             'Heart failure, unspecified',                  'Tim mạch'),
('I63',   'Nhồi máu não',                                       'Cerebral infarction',                         'Tim mạch'),
('I64',   'Đột quỵ',                                            'Stroke, not specified',                       'Tim mạch'),

-- Bệnh hô hấp
('J00',   'Viêm mũi họng cấp (cảm lạnh)',                     'Acute nasopharyngitis (common cold)',         'Hô hấp'),
('J01.9', 'Viêm xoang cấp không xác định',                     'Acute sinusitis, unspecified',               'Hô hấp'),
('J02.9', 'Viêm họng cấp không xác định',                      'Acute pharyngitis, unspecified',             'Hô hấp'),
('J03.9', 'Viêm amidan cấp không xác định',                    'Acute tonsillitis, unspecified',             'Hô hấp'),
('J06.9', 'Nhiễm trùng hô hấp trên cấp tính',                 'Acute upper respiratory infection, unspecified', 'Hô hấp'),
('J18.9', 'Viêm phổi không xác định',                          'Pneumonia, unspecified',                      'Hô hấp'),
('J20.9', 'Viêm phế quản cấp không xác định',                  'Acute bronchitis, unspecified',              'Hô hấp'),
('J30.4', 'Viêm mũi dị ứng không xác định',                   'Allergic rhinitis, unspecified',              'Hô hấp'),
('J44.1', 'Bệnh phổi tắc nghẽn mãn tính (COPD) đợt cấp',     'COPD with acute exacerbation',               'Hô hấp'),
('J45.9', 'Hen phế quản không xác định',                       'Asthma, unspecified',                         'Hô hấp'),

-- Bệnh tiêu hóa
('K21.0', 'Trào ngược dạ dày thực quản (GERD)',                'Gastro-oesophageal reflux disease',          'Tiêu hóa'),
('K25.9', 'Loét dạ dày không xác định',                        'Gastric ulcer, unspecified',                  'Tiêu hóa'),
('K29.7', 'Viêm dạ dày không xác định',                        'Gastritis, unspecified',                      'Tiêu hóa'),
('K30',   'Khó tiêu chức năng',                                 'Functional dyspepsia',                        'Tiêu hóa'),
('K58.9', 'Hội chứng ruột kích thích (IBS)',                   'Irritable bowel syndrome without diarrhoea', 'Tiêu hóa'),
('K59.0', 'Táo bón',                                            'Constipation',                                'Tiêu hóa'),
('K76.0', 'Gan nhiễm mỡ',                                       'Fatty (change of) liver',                     'Tiêu hóa'),

-- Bệnh da
('L20.9', 'Viêm da cơ địa không xác định',                    'Atopic dermatitis, unspecified',              'Da liễu'),
('L23.9', 'Viêm da tiếp xúc dị ứng',                          'Allergic contact dermatitis',                 'Da liễu'),
('L50.9', 'Mày đay không xác định',                             'Urticaria, unspecified',                      'Da liễu'),

-- Bệnh cơ xương khớp
('M13.9', 'Viêm khớp không xác định',                          'Arthritis, unspecified',                      'Cơ xương khớp'),
('M54.5', 'Đau thắt lưng',                                      'Low back pain',                               'Cơ xương khớp'),
('M79.3', 'Viêm mô bao gân',                                   'Panniculitis, unspecified',                   'Cơ xương khớp'),

-- Bệnh tiết niệu - sinh dục
('N30.0', 'Viêm bàng quang cấp',                               'Acute cystitis',                              'Tiết niệu'),
('N39.0', 'Nhiễm trùng đường tiết niệu',                       'Urinary tract infection',                     'Tiết niệu'),

-- Triệu chứng, dấu hiệu
('R05',   'Ho',                                                  'Cough',                                       'Triệu chứng'),
('R10.4', 'Đau bụng không xác định',                            'Unspecified abdominal pain',                  'Triệu chứng'),
('R11',   'Buồn nôn và nôn',                                    'Nausea and vomiting',                         'Triệu chứng'),
('R50.9', 'Sốt không xác định',                                 'Fever, unspecified',                          'Triệu chứng'),
('R51',   'Đau đầu',                                            'Headache',                                    'Triệu chứng'),
('R53',   'Mệt mỏi',                                            'Malaise and fatigue',                         'Triệu chứng'),

-- Chấn thương
('S00.9', 'Chấn thương bề mặt vùng đầu',                      'Superficial injury of head, unspecified',     'Chấn thương'),
('T78.4', 'Dị ứng không xác định',                              'Allergy, unspecified',                        'Dị ứng'),
('T88.7', 'Phản ứng bất lợi do thuốc',                         'Adverse effect of drug or medicament',        'Dị ứng'),

-- Khám sức khỏe
('Z00.0', 'Khám sức khỏe tổng quát',                           'General adult medical examination',           'Khám sức khỏe'),
('Z01.0', 'Khám mắt và thị lực',                               'Examination of eyes and vision',              'Khám sức khỏe'),
('Z13.6', 'Tầm soát bệnh tim mạch',                            'Screening for cardiovascular disorders',      'Khám sức khỏe');
