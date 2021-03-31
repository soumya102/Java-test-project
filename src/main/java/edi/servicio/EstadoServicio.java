/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edi.servicio;

import edi.dao.IEstadoDAO;
import edi.dao.hibernate.Estado;
import java.util.List;

/**
 *
 * @author alvarjor
 */
public class EstadoServicio implements IEstadoServicio {
    
    private IEstadoDAO estadoDAO;
    

    public void setEstadoDAO(IEstadoDAO estadoDAO) {
        this.estadoDAO=estadoDAO;
    }

    public String guardar(String codigo, String descripcion, String descripcionCorta, String color, int orden) {
        Estado estado;

        estado=new Estado(codigo, descripcion, descripcionCorta, color, orden);

        return this.estadoDAO.guardar(estado);
    }

    public Estado obtener(String codEstado) {
        return this.estadoDAO.obtener(codEstado);
    }

    public List<Estado> listar(String prefijo, boolean incluirSoloOrdenados) {
        return this.estadoDAO.listar(prefijo, incluirSoloOrdenados);
    }



  

}
