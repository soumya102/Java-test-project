/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edi.servicio;

import edi.dao.IParametroDAO;
import edi.dao.hibernate.Parametro;

/**
 *
 * @author alvarjor
 */
public interface IParametroServicio {

    public void setParametroDAO(IParametroDAO parametroDAO);
    
    
    public void guardar(String codParametro, double valorNum1, double valorNum2, String valorAlfa1, String valorAlfa2) throws Exception;
    public Parametro obtener(String codigo) throws Exception;
}
