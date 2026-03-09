package com.hospital.model;

/**
 * Model mã ICD-10 — danh mục chuẩn bệnh quốc tế.
 */
public class Icd10Code extends BaseModel {

    private String code;
    private String nameVi;
    private String nameEn;
    private String category;

    public Icd10Code() {}

    public Icd10Code(String code, String nameVi, String nameEn, String category) {
        this.code = code;
        this.nameVi = nameVi;
        this.nameEn = nameEn;
        this.category = category;
    }

    // ── Getters & Setters ────────────────────────────────────

    public String getCode()                    { return code; }
    public void setCode(String v)              { this.code = v; }

    public String getNameVi()                  { return nameVi; }
    public void setNameVi(String v)            { this.nameVi = v; }

    public String getNameEn()                  { return nameEn; }
    public void setNameEn(String v)            { this.nameEn = v; }

    public String getCategory()                { return category; }
    public void setCategory(String v)          { this.category = v; }

    /** Hiển thị gợi ý: "J06.9 — Nhiễm trùng hô hấp trên cấp tính" */
    public String getDisplayText() {
        return code + " — " + (nameVi != null ? nameVi : nameEn);
    }

    @Override
    public String toString() {
        return "Icd10Code{code='" + code + "', nameVi='" + nameVi + "'}";
    }
}
