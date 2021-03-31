/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edi.servicio;

import edi.dao.IClienteEANDAO;
import edi.dao.hibernate.ClienteEAN;
import java.util.List;
import mars.web.common.Utilidades;

/**
 *
 * @author alvarjor
 */
public class ClienteEANServicio implements IClienteEANServicio {
    
    private IClienteEANDAO ClienteEANDAO;


    public void setClienteEANDAO(IClienteEANDAO ClienteEANDAO) {
        this.ClienteEANDAO=ClienteEANDAO;
    }


    public String guardar(String codTerritorio, String codSoldTo, String nombreSoldTo, String codBillTo, String nombreBillTo, String ean) {
        String resultado;
        ClienteEAN ClienteEAN;

        resultado="OK";

        try {
            ClienteEAN=new ClienteEAN(codSoldTo, nombreSoldTo, codBillTo, nombreBillTo, ean, codTerritorio);
            resultado=this.ClienteEANDAO.guardar(ClienteEAN);
        } catch (Exception ex) {
            resultado=Utilidades.obtenerMsgError(ex);
        }

        return resultado;
    }


    public ClienteEAN obtener(String codSoldTo) {
        return this.ClienteEANDAO.obtener(codSoldTo);
    }


    public String eliminar(String codSoldTo) {
        return this.ClienteEANDAO.eliminar(new ClienteEAN(codSoldTo));
    }


    public List<ClienteEAN> listar(String codTerritorio, String nombre) {
        return this.ClienteEANDAO.listar(codTerritorio, nombre);
    }

}
