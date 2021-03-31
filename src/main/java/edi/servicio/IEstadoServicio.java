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
public interface IEstadoServicio {

    public void setEstadoDAO(IEstadoDAO estadoDAO);
    public String guardar(String codigo, String descripcion, String descripcionCorta, String color, int orden);
    public Estado obtener(String codEstado);
    public List<Estado> listar(String prefijo, boolean incluirSoloOrdenados);

}
