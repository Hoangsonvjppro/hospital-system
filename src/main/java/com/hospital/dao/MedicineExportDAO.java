package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;
import com.hospital.model.MedicineExport;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicineExportDAO {
    // Thực hiện xuất thuốc và trừ kho (Trong StockTransaction)
    public boolean exportAndDeductMedicine(MedicineExport export) throws Exception {
        Connection con=null;
        try {
            con= DatabaseConfig.getInstance().getConnection();
            con.setAutoCommit(false);//transaction

            String query="Select stock_qty from Medicine where is_active=true and medicine_id=? for update";
            int stockBefore;
            try(PreparedStatement ps=con.prepareStatement(query)) {
                ps.setInt(1,export.getMedicineId());
                try(ResultSet rs=ps.executeQuery()) {
                    if(rs.next()){
                        stockBefore=rs.getInt("stock_qty");
                    }
                    else {
                        throw new Exception("Không tìm thấy thuốc ID: " + export.getMedicineId());
                    }
                }

            }
            if(stockBefore<export.getQuantityExported()){
                throw new Exception("Tồn kho ko đủ để xuất");
            }
            //Trừ kho thuốc
            int stockAfter=stockBefore-export.getQuantityExported();
            String sql="Update Medicine set stock_qty=? where medicine_id=?";
            try(PreparedStatement ps=con.prepareStatement(sql)) {
                ps.setInt(1,stockAfter);
                ps.setInt(2,export.getMedicineId());
                ps.executeUpdate();
            }
            //Insert dữ liệu vào bảng StockTransaction
            String insert="INSERT INTO StockTransaction "  +
                    "(medicine_id, transaction_type, quantity, stock_before, stock_after, reference_type, reference_id, created_by, created_at) "+
                    "VALUES (?, 'EXPORT', ?, ?, ?, 'PRESCRIPTION', ?, ?, ?)";
            try(PreparedStatement ps=con.prepareStatement(insert)) {
                ps.setInt(1,export.getMedicineId());
                ps.setInt(2,-export.getQuantityExported());
                ps.setInt(3,stockBefore);
                ps.setInt(4,stockAfter);
                ps.setInt(5,export.getPrescriptionDetailId());
                ps.setInt(6,export.getPharmacistId());
                ps.setTimestamp(7, Timestamp.valueOf(export.getExportDate()));
                ps.executeUpdate();
            }
            con.commit();
            return true;

        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                }
                catch (SQLException ignored) {}
            }
            throw new DataAccessException("Lỗi Database khi xuất kho: " + e.getMessage(), e);
        }finally {
            if(con!=null){
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException ignored) {}
            }
        }
    }
    public List<MedicineExport> getExportHistory(){
        List<MedicineExport> arr=new ArrayList<>();
        Connection con=null;
        String sql="Select * from StockTransaction where transaction_type='EXPORT' and reference_type='PRESCRIPTION'";
        try {
            con=DatabaseConfig.getInstance().getConnection();
            try(Statement stm=con.createStatement()) {
                ResultSet rs=stm.executeQuery(sql);
                while (rs.next()){
                    MedicineExport ex=new MedicineExport();
                    ex.setId(rs.getInt("transaction_id"));
                    ex.setPrescriptionDetailId(rs.getInt("reference_id"));
                    ex.setMedicineId(rs.getInt("medicine_id"));
                    ex.setQuantityExported(Math.abs(rs.getInt("quantity")));//Chuyển thành số dương
                    ex.setExportDate(rs.getTimestamp("created_at").toLocalDateTime());
                    ex.setPharmacistId(rs.getInt("created_by"));
                    arr.add(ex);
                }
            }
        } catch (SQLException e) {
                throw new DataAccessException("Lỗi truy vấn lịch sử xuất thuốc",e);
        }
        return arr;
    }
}
