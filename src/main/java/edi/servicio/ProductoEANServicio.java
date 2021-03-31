/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edi.servicio;

import edi.dao.IProductoEANDAO;
import edi.dao.hibernate.ProductoEAN;
import java.util.List;

/**
 *
 * @author alvarjor
 */
public class ProductoEANServicio implements IProductoEANServicio {
    
    private IProductoEANDAO productoEANDAO;


    public void setProductoEANDAO(IProductoEANDAO productoEANDAO) {
        this.productoEANDAO=productoEANDAO;
    }


    public String guardar(String codProductoATLAS, String descripcion, String codEAN, int CantUndInternas) {
        String resultado;
        ProductoEAN productoEAN;

        resultado="OK";

        try {
            productoEAN=new ProductoEAN(codProductoATLAS, descripcion, codEAN, CantUndInternas);
            resultado=this.productoEANDAO.guardar(productoEAN);
        } catch (Exception ex) {
            resultado=ex.getMessage();
        }

        return resultado;
    }


    public ProductoEAN obtener(String codProductoATLAS) {
        return this.productoEANDAO.obtener(codProductoATLAS);
    }


    public String eliminar(String codProductoATLAS) {
        return this.productoEANDAO.eliminar(new ProductoEAN(codProductoATLAS));
    }


    public List<ProductoEAN> listar(String descripcion) {
        return this.productoEANDAO.listar(descripcion);
    }

}
