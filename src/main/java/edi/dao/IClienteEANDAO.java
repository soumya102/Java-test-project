/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edi.dao;

import edi.dao.hibernate.ClienteEAN;
import java.util.List;

/**
 *
 * @author alvarjor
 */
public interface IClienteEANDAO {

    public String guardar(ClienteEAN ClienteEAN);
    public ClienteEAN obtener(String codSoldTo);
    public String eliminar(ClienteEAN ClienteEAN);
    public List<ClienteEAN> listar(String codTerritorio, String nombre);

}
