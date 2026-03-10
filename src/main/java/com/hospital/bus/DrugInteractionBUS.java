package com.hospital.bus;

import com.hospital.dao.DrugInteractionDAO;
import com.hospital.model.DrugInteraction;

import java.util.List;

/**
 * Business logic layer cho tương tác thuốc (DrugInteraction).
 * Chủ yếu là tra cứu tương tác giữa các thuốc.
 */
public class DrugInteractionBUS {

    private final DrugInteractionDAO interactionDAO = new DrugInteractionDAO();

    public List<DrugInteraction> findAll() {
        return interactionDAO.findAll();
    }

    public List<DrugInteraction> findInteractions(List<Integer> medicineIds) {
        return interactionDAO.findInteractions(medicineIds);
    }
}
