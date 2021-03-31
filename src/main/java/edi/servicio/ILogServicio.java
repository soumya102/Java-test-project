/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edi.servicio;

import edi.dao.ILogDAO;
import java.util.Date;
import java.util.List;

/**
 *
 * @author alvarjor
 */
public interface ILogServicio {

    public void setLogDAO(ILogDAO logDAO);
    public List listar(String codTerritorio, Date fechaReporte) throws Exception;
}
