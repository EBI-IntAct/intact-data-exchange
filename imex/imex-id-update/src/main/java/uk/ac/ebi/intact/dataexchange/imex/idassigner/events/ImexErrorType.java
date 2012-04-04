package uk.ac.ebi.intact.dataexchange.imex.idassigner.events;

/**
 * List the possible error types
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>04/04/12</pre>
 */

public enum ImexErrorType {

    user_not_authorized, operation_not_valid, identifier_missing, identifier_unknown, no_record, no_record_created,
    status_unknown, no_IMEX_id, unknown_user, unknown_group, operation_not_supported, internal_server_error,
    publication_imex_conflict, interaction_imex_conflict, experiment_imex_conflict, imex_not_recognized, imex_in_imexCentral_not_in_intact
}
