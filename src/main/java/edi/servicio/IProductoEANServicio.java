/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edi.servicio;

import edi.dao.hibernate.ProductoEAN;
import java.util.List;

/**
 *
 * @author alvarjor
 */
public interface IProductoEANServicio {

    public String guardar(String codProductoATLAS, String descripcion, String codEAN, int CantUndInternas);
    public ProductoEAN obtener(String codProductoATLAS);
    public String eliminar(String codProductoATLAS);
    public List<ProductoEAN> listar(String descripcion);


}
