package dss.cadeiaRestaurantesLN.subsistemaRestaurantes;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Interface for managing restaurants and employees subsystem.
 * Handles employee management, messaging, and performance indicators.
 */
public interface IGestRestaurantes {
    /**
     * Retrieves performance indicators for restaurants within a date range.
     * @param dataInicio Start date for the report
     * @param dataFim End date for the report
     * @param funcionarioId ID of the employee requesting the report
     * @param restaurantesId List of restaurant IDs to include in the report
     * @return List of formatted performance indicator strings
     */
    List<String> consultarIndicadoresDesempenho(LocalDate dataInicio, LocalDate dataFim, Integer funcionarioId, List<Integer> restaurantesId);

    /**
     * Sends a message to specific positions in selected restaurants.
     * @param funcionarioId ID of the employee sending the message
     * @param mensagem Message content
     * @param postos List of position types to receive the message
     * @param restaurantesId List of restaurant IDs to send the message to
     */
    void enviarMensagem(Integer funcionarioId, String mensagem, List<String> postos, List<Integer> restaurantesId);

    /**
     * Checks if an employee is a general manager.
     * @param funcionarioId Employee ID to check
     * @return true if employee is a general manager, false otherwise
     */
    boolean isGestorGeral(Integer funcionarioId);

    /**
     * Checks if an employee exists in the system.
     * @param funcionarioId Employee ID to check
     * @return true if employee exists, false otherwise
     */
    boolean existeFuncionario(Integer funcionarioId);

    /**
     * Checks if an employee has a specific role.
     * @param funcionarioId Employee ID to check
     * @param role Role name to verify
     * @return true if employee has the specified role, false otherwise
     */
    boolean isFuncionarioRole(Integer funcionarioId, String role);

    /**
     * Gets the names of restaurants accessible to an employee.
     * @param idFuncionario Employee ID
     * @return List of restaurant names
     */
    List<String> getNomesRestaurantes(Integer idFuncionario);

    /**
     * Gets valid position types that a manager can send messages to.
     * @param idFuncionario Manager's employee ID
     * @return Set of valid position type names
     */
    Set<String> getTiposPostoValidosParaGestor(Integer idFuncionario);

    /**
     * Retrieves messages for an employee's position.
     * @param idFuncionario Employee ID
     * @return List of messages for the employee's position
     */
    List<String> getMensagensPosto(Integer idFuncionario);

    /**
     * Gets an employee's profile information.
     * @param idFuncionario Employee ID
     * @return List of profile information strings
     */
    List<String> getPerfilFuncionario(Integer idFuncionario);

    /**
     * Gets the restaurant ID where an employee works.
     * @param idFuncionario Employee ID
     * @return Restaurant ID, or null if employee is a general manager
     */
    Integer getRestauranteIdPorFuncionario(Integer idFuncionario);

    /**
     * Gets names of all restaurants in the chain.
     * @return List of all restaurant names
     */
    List<String> getNomesTodosRestaurantes();
}