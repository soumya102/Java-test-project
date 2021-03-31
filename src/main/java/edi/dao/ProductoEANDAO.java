/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edi.dao;

import edi.dao.hibernate.ProductoEAN;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
/**
 *
 * @author alvarjor
 */
public class ProductoEANDAO implements IProductoEANDAO {
    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }


    public String guardar(ProductoEAN productoEAN) {
        String resultado;

        resultado="OK";

        try {
            sessionFactory.getCurrentSession().saveOrUpdate(productoEAN);
        } catch (HibernateException ie) {
            resultado=ie.getMessage();
        }

        return resultado;
    }


    public ProductoEAN obtener(String codProductoATLAS)  {
        return (ProductoEAN)sessionFactory.getCurrentSession().load(ProductoEAN.class, codProductoATLAS);
    }


    public String eliminar(ProductoEAN productoEAN) {
        String resultado;

        resultado = "OK";

        try {
            sessionFactory.getCurrentSession().delete(productoEAN);
        } catch (HibernateException ie) {
            resultado = ie.getMessage();
        }

        return resultado;
    }


    public List<ProductoEAN> listar(String descripcion) {
         Criteria criteria;

         criteria= sessionFactory.getCurrentSession().createCriteria(ProductoEAN.class);
         criteria.add(Expression.like("descripcion", descripcion, MatchMode.ANYWHERE).ignoreCase());
         criteria.addOrder(Order.asc("descripcion"));

         return criteria.list();
    }
}
