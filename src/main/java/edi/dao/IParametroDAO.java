/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edi.dao;

import edi.dao.hibernate.Parametro;
import java.util.List;

/**
 *
 * @author alvarjor
 */
public interface IParametroDAO {

    public Parametro obtener(String codigo) throws Exception;
    public List listar(String prefijo) throws Exception;
    public void guardar(Parametro parametro);

}
