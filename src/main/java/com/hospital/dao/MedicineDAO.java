package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.model.Account;
import com.hospital.model.Medicine;
import com.hospital.util.AppUtils;

import javax.swing.*;
import javax.xml.transform.Result;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicineDAO implements BaseDAO<Medicine>{
    private Connection getConnection() throws SQLException{
        return DatabaseConfig.getInstance().getConnection();
    }

    @Override
    public Medicine findById(int id) {
        String sql="Select * from Medicine where medicine_id=?";
        try(PreparedStatement ps=getConnection().prepareStatement(sql)) {
            ps.setInt(1,id);
            ResultSet rs=ps.executeQuery();
            if(rs.next()){
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            AppUtils.showError(null,"Lỗi database");
        }
        return null;
    }
    public List<Medicine> findByName(String keyword){
        List<Medicine> arr=new ArrayList<>();
        String sql="Select * from Medicine where is_active=true and medicine_name like ?";
        try(PreparedStatement ps=getConnection().prepareStatement(sql)) {
            ps.setString(1,"%"+keyword+"%");
            ResultSet rs=ps.executeQuery();
            while (rs.next()){
               arr.add(mapResultSet(rs));
            }
        }catch (SQLException e){
            AppUtils.showError(null,"Không thể tìm");
        }
        return arr;
    }

    @Override
    public List<Medicine> findAll() {
        List<Medicine> arr=new ArrayList<>();
        String sql="Select * from Medicine";
        try (Statement stm = getConnection().createStatement()) {
            ResultSet rs = stm.executeQuery(sql);
            while (rs.next()){
                arr.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            AppUtils.showError(null,"Lỗi database");
        }
        return arr;
    }

    @Override
    public boolean insert(Medicine entity) {
        String sql="INSERT INTO Medicine (medicine_name, unit, cost_price, sell_price, stock_qty, min_threshold, expiry_date, description, is_active) VALUES (?,?,?,?,?,?,?,?,?)";
        try(PreparedStatement ps=getConnection().prepareStatement(sql)){
            ps.setString(1,entity.getMedicineName());
            ps.setString(2,entity.getUnit());
            ps.setDouble(3,entity.getCostPrice());
            ps.setDouble(4,entity.getSellPrice());
            ps.setInt(5,entity.getStockQty());
            ps.setInt(6,entity.getMinThreshold());
            if(entity.getExpiryDate()!=null){
                ps.setDate(7,java.sql.Date.valueOf(entity.getExpiryDate()));
            }
            else ps.setNull(7,Types.DATE);
            ps.setString(8, entity.getDescription());
            ps.setBoolean(9, entity.isActive());
            return ps.executeUpdate()>0;
        }catch (SQLException e){
            AppUtils.showError(null,"Lỗi không thể thêm");
        }
        return false;
    }

    @Override
    public boolean update(Medicine entity) {
        String sql="UPDATE Medicine SET medicine_name=?, unit=?, cost_price=?, sell_price=?, stock_qty=?, min_threshold=?, expiry_date=?, description=?, is_active=? WHERE medicine_id=?";
        try(PreparedStatement ps=getConnection().prepareStatement(sql)) {
            ps.setString(1,entity.getMedicineName());
            ps.setString(2,entity.getUnit());
            ps.setDouble(3,entity.getCostPrice());
            ps.setDouble(4,entity.getSellPrice());
            ps.setInt(5,entity.getStockQty());
            ps.setInt(6,entity.getMinThreshold());
            if(entity.getExpiryDate()!=null){
                ps.setDate(7,java.sql.Date.valueOf(entity.getExpiryDate()));
            }
            else ps.setNull(7,Types.DATE);
            ps.setString(8, entity.getDescription());
            ps.setBoolean(9, entity.isActive());
            ps.setInt(10,entity.getId());
            return ps.executeUpdate()>0;

        }catch (SQLException e){
            AppUtils.showError(null,"Lỗi không thể cập nhật");
        }

        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql="Delete from Medicine where medicine_id=?";
        try(PreparedStatement ps=getConnection().prepareStatement(sql)) {
            ps.setInt(1,id);
            return ps.executeUpdate()>0;
        }catch (SQLException e){
            AppUtils.showError(null,"Không thể xoá thuốc");
        }
        return false;
    }


    private Medicine mapResultSet(ResultSet rs) throws SQLException {
        Medicine thuoc=new Medicine();
        thuoc.setId(rs.getInt("medicine_id"));
        thuoc.setMedicineName(rs.getString("medicine_name"));
        thuoc.setUnit(rs.getString("unit"));
        thuoc.setCostPrice(rs.getDouble("cost_price"));
        thuoc.setSellPrice(rs.getDouble("sell_price"));
        thuoc.setStockQty(rs.getInt("stock_qty"));
        thuoc.setMinThreshold(rs.getInt("min_threshold"));
        if (rs.getDate("expiry_date") != null) {
            thuoc.setExpiryDate(rs.getDate("expiry_date").toLocalDate());
        }
        thuoc.setDescription(rs.getString("description"));
        thuoc.setActive(rs.getBoolean("is_active"));
        return thuoc;

    }
    }
