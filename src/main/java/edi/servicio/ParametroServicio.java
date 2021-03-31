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
public class ParametroServicio implements IParametroServicio {
    
    private IParametroDAO parametroDAO;

    public void setParametroDAO(IParametroDAO parametroDAO) {
        this.parametroDAO = parametroDAO;
    }

    public Parametro obtener(String codigo) throws Exception {
        return this.parametroDAO.obtener(codigo);
    }    
    
    public void guardar(String codParametro, double valorNum1, double valorNum2, String valorAlfa1, String valorAlfa2) throws Exception {
        this.parametroDAO.guardar(new Parametro(codParametro, valorNum1, valorNum2, valorAlfa1, valorAlfa2));
    }
}
