package uk.ac.ebi.intact.dataexchange.enricher.standard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import psidev.psi.mi.jami.enricher.ComplexEnricher;
import psidev.psi.mi.jami.enricher.*;
import psidev.psi.mi.jami.enricher.InteractorEnricher;
import psidev.psi.mi.jami.enricher.InteractorPoolEnricher;
import psidev.psi.mi.jami.enricher.ProteinEnricher;
import psidev.psi.mi.jami.enricher.exception.EnricherException;
import psidev.psi.mi.jami.enricher.impl.AbstractInteractorEnricher;
import psidev.psi.mi.jami.model.*;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;

import java.util.Collection;

/**
 * General enricher for interactors that can use sub enrichers for enriching specific interactors
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>11/02/14</pre>
 */
@Component(value = "intactCompositeInteractorEnricher")
@Lazy
@Scope( BeanDefinition.SCOPE_PROTOTYPE )
public class CompositeInteractorEnricher extends psidev.psi.mi.jami.enricher.impl.CompositeInteractorEnricher {

    @Autowired
    private EnricherContext enricherContext;

    @Autowired
    public CompositeInteractorEnricher(@Qualifier("intactInteractorEnricher") InteractorEnricher
                                                   interactorEnricher){
        super((AbstractInteractorEnricher)interactorEnricher);
    }

    public InteractorPoolEnricher getInteractorPoolEnricher() {
        if (super.getInteractorPoolEnricher() == null){
            super.setInteractorPoolEnricher((psidev.psi.mi.jami.enricher.InteractorPoolEnricher)
                    ApplicationContextProvider.getBean("intactInteractorPoolEnricher"));
        }
        return super.getInteractorPoolEnricher();
    }

    public ProteinEnricher getProteinEnricher() {
        if (super.getProteinEnricher() == null){
            super.setProteinEnricher((ProteinEnricher)
                    ApplicationContextProvider.getBean("intactProteinEnricher"));
        }
        return super.getProteinEnricher();
    }


    public InteractorEnricher<BioactiveEntity> getBioactiveEntityEnricher() {
        if (super.getBioactiveEntityEnricher() == null){
            super.setBioactiveEntityEnricher((InteractorEnricher<BioactiveEntity>)
                    ApplicationContextProvider.getBean("intactBioactiveEntityEnricher"));
        }
        return super.getBioactiveEntityEnricher();
    }

    public InteractorEnricher<Gene> getGeneEnricher() {
        if (super.getGeneEnricher() == null){
            super.setGeneEnricher((InteractorEnricher<Gene>)
                    ApplicationContextProvider.getBean("intactGeneEnricher"));
        }
        return super.getGeneEnricher();
    }

    public InteractorEnricher<NucleicAcid> getNucleicAcidEnricher() {
        if (super.getGeneEnricher() == null){
            super.setNucleicAcidEnricher((InteractorEnricher<NucleicAcid>)
                    ApplicationContextProvider.getBean("intactNucleicAcidEnricher"));
        }
        return super.getNucleicAcidEnricher();
    }

    public ComplexEnricher getComplexEnricher() {
        if (super.getComplexEnricher() == null){
            super.setComplexEnricher((ComplexEnricher)
                    ApplicationContextProvider.getBean("intactComplexEnricher"));
        }
        return super.getComplexEnricher();
    }

    public void enrich(Interactor object) throws EnricherException {
        if(object == null)
            throw new IllegalArgumentException("Cannot enrich a null interactor.");
        if (object instanceof Polymer){
            if (object instanceof Protein
                    && enricherContext.getConfig().isUpdateProteins()
                    && getProteinEnricher() != null){
               getProteinEnricher().enrich((Protein)object);
            }
            else if (object instanceof NucleicAcid
                    && enricherContext.getConfig().isUpdateNucleicAcids()
                    && getNucleicAcidEnricher() != null){
                getNucleicAcidEnricher().enrich((NucleicAcid) object);
            }
            else if (getPolymerBaseEnricher() != null){
               getPolymerBaseEnricher().enrich((Polymer)object);
            }
            else{
                getInteractorBaseEnricher().enrich(object);
            }
        }
        else if (object instanceof Gene
                && enricherContext.getConfig().isUpdateGenes()
                && getGeneEnricher() != null){
            getGeneEnricher().enrich((Gene)object);
        }
        else if (object instanceof BioactiveEntity
                && enricherContext.getConfig().isUpdateSmallMolecules()
                && getBioactiveEntityEnricher()!= null){
             getBioactiveEntityEnricher().enrich((BioactiveEntity)object);
        }
        else if (object instanceof Complex
                && enricherContext.getConfig().isUpdateComplexes()
                && getComplexEnricher() != null){
           getComplexEnricher().enrich((Complex)object);
        }
        else if (object instanceof InteractorPool
                && enricherContext.getConfig().isUpdateInteractorPool()
                && getInteractorPoolEnricher() != null){
            getInteractorPoolEnricher().enrich((InteractorPool)object);
        }
        else{
            getInteractorBaseEnricher().enrich(object);
        }
    }

    public void enrich(Collection<Interactor> objects) throws EnricherException {
        if(objects == null)
            throw new IllegalArgumentException("Cannot enrich a null collection of interactors.");

        for (Interactor object : objects){
            enrich(object);
        }
    }

    @Override
    public OrganismEnricher getOrganismEnricher() {
        return (OrganismEnricher) ApplicationContextProvider.getBean("intactBioSourceEnricher");
    }

    @Override
    public CvTermEnricher<CvTerm> getCvTermEnricher() {
        return (CvTermEnricher<CvTerm>) ApplicationContextProvider.getBean("miCvObjectEnricher");
    }
}
