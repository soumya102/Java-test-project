/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edi.dao;

import edi.dao.hibernate.ClienteEAN;
import java.util.List;
import mars.web.common.Utilidades;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
/**
 *
 * @author alvarjor
 */
public class ClienteEANDAO implements IClienteEANDAO {
    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }


    public String guardar(ClienteEAN ClienteEAN) {
        String resultado;

        resultado="OK";

        try {
            sessionFactory.getCurrentSession().saveOrUpdate(ClienteEAN);
        } catch (HibernateException ex) {
            resultado=Utilidades.obtenerMsgError(ex);
        }

        return resultado;
    }


    public ClienteEAN obtener(String codSoldTo)  {
        try {
            return (ClienteEAN)sessionFactory.getCurrentSession().get(ClienteEAN.class, codSoldTo);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        return null;
    }


    public String eliminar(ClienteEAN ClienteEAN) {
        String resultado;

        resultado = "OK";

        try {
            sessionFactory.getCurrentSession().delete(ClienteEAN);
        } catch (HibernateException ie) {
            resultado = ie.getMessage();
        }

        return resultado;
    }


    public List<ClienteEAN> listar(String codTerritorio, String nombre) {
         Criteria criteria;

         criteria= sessionFactory.getCurrentSession().createCriteria(ClienteEAN.class);
         if (!codTerritorio.isEmpty()) {
            criteria.add(Expression.eq("codTerritorio", codTerritorio));
         }
         criteria.add(Expression.like("nombreSoldTo", nombre, MatchMode.ANYWHERE).ignoreCase());
         criteria.addOrder(Order.asc("nombreSoldTo"));

         return criteria.list();
    }
}
