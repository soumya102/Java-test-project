/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edi.dao;

import edi.dao.hibernate.ProductoEAN;
import java.util.List;

/**
 *
 * @author alvarjor
 */
public interface IProductoEANDAO {

    public String guardar(ProductoEAN productoEAN);
    public ProductoEAN obtener(String codProductoATLAS);
    public String eliminar(ProductoEAN productoEAN);
    public List<ProductoEAN> listar(String descripcion);

}
