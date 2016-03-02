package org.broadinstitute.hellbender.tools.walkers.annotator;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypeBuilder;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFormatHeaderLine;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.broadinstitute.hellbender.engine.ReferenceContext;
import org.broadinstitute.hellbender.utils.Utils;
import org.broadinstitute.hellbender.utils.genotyper.PerReadAlleleLikelihoodMap;
import org.broadinstitute.hellbender.utils.variant.GATKVCFConstants;
import org.broadinstitute.hellbender.utils.variant.GATKVCFHeaderLines;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Number of forward and reverse reads that support REF and ALT alleles
 *
 * <p>Strand bias is a type of sequencing bias in which one DNA strand is favored over the other, which can result in incorrect evaluation of the amount of evidence observed for one allele vs. the other. The StrandBiasBySample annotation produces read counts per allele and per strand that are used by other annotation modules (FisherStrand and StrandOddsRatio) to estimate strand bias using statistical approaches.
 *
 * <p>This annotation produces 4 values, corresponding to the number of reads that support the following (in that order):</p>
 * <ul>
 *     <li>the reference allele on the forward strand</li>
 *     <li>the reference allele on the reverse strand</li>
 *     <li>the alternate allele on the forward strand</li>
 *     <li>the alternate allele on the reverse strand</li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>GT:AD:GQ:PL:SB  0/1:53,51:99:1758,0,1835:23,30,33,18</pre>
 * <p>In this example, the reference allele is supported by 23 forward reads and 30 reverse reads, the alternate allele is supported by 33 forward reads and 18 reverse reads.</p>
 *
 * <h3>Caveats</h3>
 * <ul>
 *     <li>This annotation can only be generated by HaplotypeCaller (it will not work when called from VariantAnnotator).</li>
 * </ul>
 *
 * <h3>Related annotations</h3>
 * <ul>
 *     <li><b><a href="https://www.broadinstitute.org/gatk/guide/tooldocs/org_broadinstitute_gatk_tools_walkers_annotator_FisherStrand.php">FisherStrand</a></b> uses Fisher's Exact Test to evaluate strand bias.</li>
 *     <li><b><a href="https://www.broadinstitute.org/gatk/guide/tooldocs/org_broadinstitute_gatk_tools_walkers_annotator_StrandOddsRatio.php">StrandOddsRatio</a></b> is an updated form of FisherStrand that uses a symmetric odds ratio calculation.</li>
 * </ul>
 */
public final class StrandBiasBySample extends GenotypeAnnotation {
    private final static Logger logger = LogManager.getLogger(StrandBiasBySample.class);

    @Override
    public void annotate(final ReferenceContext ref,
                         final VariantContext vc,
                         final Genotype g,
                         final GenotypeBuilder gb,
                         final PerReadAlleleLikelihoodMap alleleLikelihoodMap) {
        Utils.nonNull(vc);
        Utils.nonNull(g);
        Utils.nonNull(gb);

        if ( alleleLikelihoodMap == null || !g.isCalled() ) {
            logger.warn("Annotation will not be calculated, genotype is not called or alleleLikelihoodMap is null");
            return;
        }

        final int[][] table = FisherStrand.getContingencyTable(Collections.singletonMap(g.getSampleName(), alleleLikelihoodMap), vc, 0);

        gb.attribute(GATKVCFConstants.STRAND_BIAS_BY_SAMPLE_KEY, StrandBiasTableUtils.getContingencyArray(table));
    }

    @Override
    public List<String> getKeyNames() {
        return Collections.singletonList(GATKVCFConstants.STRAND_BIAS_BY_SAMPLE_KEY);
    }

    @Override
    public List<VCFFormatHeaderLine> getDescriptions() {
        return Collections.singletonList(GATKVCFHeaderLines.getFormatLine(getKeyNames().get(0)));
    }
}

