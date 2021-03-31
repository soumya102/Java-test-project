/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edi.servicio;

import edi.dao.hibernate.ClienteEAN;
import java.util.List;

/**
 *
 * @author alvarjor
 */
public interface IClienteEANServicio {

    public String guardar(String codTerritorio, String codSoldTo, String nombreSoldTo, String codBillTo, String nombreBillTo, String ean);
    public ClienteEAN obtener(String codSoldTo);
    public String eliminar(String codSoldTo);
    public List<ClienteEAN> listar(String codTerritorio, String nombre);


}
