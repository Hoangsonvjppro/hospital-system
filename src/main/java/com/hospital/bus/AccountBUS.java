package com.hospital.bus;

import com.hospital.dao.AccountDAO;
import com.hospital.dao.BaseDAO;
import com.hospital.model.Account;
import org.mindrot.jbcrypt.BCrypt;

public class AccountBUS extends BaseBUS<Account>{
    AccountDAO dao=new AccountDAO();

    public AccountBUS(BaseDAO<Account> dao) {
        super(dao);
    }


    @Override
    protected boolean validate(Account entity) {
        if(entity.getUsername()==null||entity.getUsername().trim().isEmpty()){
            System.err.println("Lỗi: tên không được để trống");
            return false;
        }
        if(entity.getFullName()==null||entity.getFullName().trim().isEmpty()){
            System.err.println("Lỗi: Họ tên đang để trống");
        }
        if(entity.getEmail()==null||entity.getEmail().trim().isEmpty()){
            System.err.println("Lỗi: Chưa có email");
            return false;
        }
        if(entity.getPhone()==null||entity.getPhone().trim().isEmpty()){
            System.err.println("Lỗi: Không có SĐT");
        }
        if(entity.getRole()<=0){
            System.err.println("Lỗi: tài khoản chưa được phân quyền");
        }
        return true;
    }
    public boolean strongPassword(String password){
        if(password==null||password.length()<8){
            return false;
        }
        String regex="^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$";
        return password.matches(regex);

    }
    public boolean usernameExists(String username){
        //gia su tang DAO co ham check duplicate
        return false;
    }
    public boolean createAccount(Account account,String password){
        if(!validate(account)){
            return false;
        }
        if(usernameExists(account.getUsername())){
            System.err.println("Lỗi: Tên đăng nhập '" + account.getUsername() + "' đã tồn tại!");
            return false;
        }
        if(!strongPassword(password)){
            System.err.println("Lỗi: Mật khẩu không đủ mạnh! Yêu cầu ít nhất 8 ký tự, gồm chữ hoa, chữ thường và số.");
            return false;
        }
        String hashedPassword=BCrypt.hashpw(password, BCrypt.gensalt(5));
        account.setPassword(hashedPassword);
        return dao.insert(account);
    }
    public Account login(String username,String password){
       Account acc=dao.findUsername();
       if(acc!=null&&BCrypt.checkpw(password,acc.getPassword())){
           if(!acc.isActive()){
               System.err.println("Tài khoản không còn tồn tại");
               return null;
           }
       }
       return acc;

    }
}
