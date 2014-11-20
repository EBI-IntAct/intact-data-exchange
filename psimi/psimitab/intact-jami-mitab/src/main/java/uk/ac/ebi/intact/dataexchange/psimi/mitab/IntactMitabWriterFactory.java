package uk.ac.ebi.intact.dataexchange.psimi.mitab;

import psidev.psi.mi.jami.datasource.InteractionWriter;
import psidev.psi.mi.jami.model.ComplexType;
import psidev.psi.mi.jami.model.InteractionCategory;
import psidev.psi.mi.jami.tab.MitabVersion;
import psidev.psi.mi.jami.tab.extension.factory.MitabWriterFactory;
import psidev.psi.mi.jami.tab.io.writer.*;
import uk.ac.ebi.intact.dataexchange.psimi.mitab.writer.*;

/**
 * Intact Factory for creating a mitab writer
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>02/05/14</pre>
 */

public class IntactMitabWriterFactory {

    private static final IntactMitabWriterFactory instance = new IntactMitabWriterFactory();

    private IntactMitabWriterFactory(){
    }

    public static IntactMitabWriterFactory getInstance() {
        return instance;
    }

    public InteractionWriter createMitabWriter(InteractionCategory interactionCategory, ComplexType complexType,
                                                  MitabVersion version, boolean extended){
        switch (complexType){
            case binary:
                return createMitabBinaryWriter(interactionCategory, version, extended);
            default:
                return createMitabWriter(interactionCategory, version, extended);
        }
    }

    public InteractionWriter createMitabBinaryWriter(InteractionCategory interactionCategory, MitabVersion version, boolean extended){
        if (interactionCategory == null){
            interactionCategory = InteractionCategory.mixed;
        }

        MitabWriterFactory mitabFactory = MitabWriterFactory.getInstance();

        if (extended){
            return mitabFactory.createMitabBinaryWriter(interactionCategory, version, extended);
        }
        else{
            switch (version){
                case v2_5:
                    switch (interactionCategory){
                        case evidence:
                            return new Mitab25IntactBinaryEvidenceWriter();
                        case modelled:
                            return new Mitab25ModelledBinaryWriter();
                        case basic:
                            return new LightMitab25BinaryWriter();
                        case mixed:
                            return new Mitab25BinaryWriter();
                        default:
                            throw new IllegalArgumentException("Cannot create a MITAB writer for Interaction category: "+interactionCategory);
                    }
                case v2_6:
                    switch (interactionCategory){
                        case evidence:
                            return new Mitab26IntactBinaryEvidenceWriter();
                        case modelled:
                            return new Mitab26ModelledBinaryWriter();
                        case basic:
                            return new LightMitab25BinaryWriter();
                        case mixed:
                            return new Mitab26BinaryWriter();
                        default:
                            throw new IllegalArgumentException("Cannot create a MITAB writer for Interaction category: "+interactionCategory);
                    }
                default:
                    switch (interactionCategory){
                        case evidence:
                            return new Mitab27IntactBinaryEvidenceWriter();
                        case modelled:
                            return new Mitab27ModelledBinaryWriter();
                        case basic:
                            return new LightMitab27BinaryWriter();
                        case mixed:
                            return new Mitab27BinaryWriter();
                        default:
                            throw new IllegalArgumentException("Cannot create a MITAB writer for Interaction category: "+interactionCategory);
                    }
            }
        }
    }

    public InteractionWriter createMitabWriter(InteractionCategory interactionCategory, MitabVersion version, boolean extended){
        if (interactionCategory == null){
            interactionCategory = InteractionCategory.mixed;
        }

        MitabWriterFactory mitabFactory = MitabWriterFactory.getInstance();

        if (extended){
            return mitabFactory.createMitabWriter(interactionCategory, version, extended);
        }
        else{
            switch (version){
                case v2_5:
                    switch (interactionCategory){
                        case evidence:
                            return new Mitab25IntactEvidenceWriter();
                        case modelled:
                            return new Mitab25ModelledWriter();
                        case basic:
                            return new LightMitab25Writer();
                        case mixed:
                            return new Mitab25Writer();
                        default:
                            throw new IllegalArgumentException("Cannot create a MITAB writer for Interaction category: "+interactionCategory);
                    }
                case v2_6:
                    switch (interactionCategory){
                        case evidence:
                            return new Mitab26IntactEvidenceWriter();
                        case modelled:
                            return new Mitab26ModelledWriter();
                        case basic:
                            return new LightMitab25Writer();
                        case mixed:
                            return new Mitab26Writer();
                        default:
                            throw new IllegalArgumentException("Cannot create a MITAB writer for Interaction category: "+interactionCategory);
                    }
                default:
                    switch (interactionCategory){
                        case evidence:
                            return new Mitab27IntactEvidenceWriter();
                        case modelled:
                            return new Mitab27ModelledWriter();
                        case basic:
                            return new LightMitab27Writer();
                        case mixed:
                            return new Mitab27Writer();
                        default:
                            throw new IllegalArgumentException("Cannot create a MITAB writer for Interaction category: "+interactionCategory);
                    }
            }
        }
    }
}
