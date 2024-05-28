package DAO;

import models.Admin;

public interface DaoAdmin {
    boolean insert(Admin admin, DAOManager dao);
    boolean update(Admin admin, DAOManager dao);
    Admin readById(int id, DAOManager dao);
    Admin readByEmail(String email, DAOManager dao);
    Admin readByEmailAndPass(String email, String pass, DAOManager dao);
}
