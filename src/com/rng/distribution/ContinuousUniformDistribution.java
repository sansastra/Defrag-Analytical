package com.rng.distribution;

import com.rng.Distribution;
import org.uncommons.maths.random.ContinuousUniformGenerator;
import org.uncommons.maths.random.MersenneTwisterRNG;

/**
 * Class for continuous uniform distribution
 *
 * @author Fran Carpio
 */
public class ContinuousUniformDistribution extends Distribution {

    private ContinuousUniformGenerator continuousUniformGenerator;
    private final String NAME = "UNIFORM";

    /**
     * Constructor class
     *
     * @param min  min value
     * @param max  max value
     * @param seed initial seed value
     */
    public ContinuousUniformDistribution(double min, double max, byte[] seed) {
        super(min, max, seed);
        MersenneTwisterRNG rng = new MersenneTwisterRNG(seed);
        continuousUniformGenerator = new ContinuousUniformGenerator(min, max, rng);
    }

    /**
     * Get next value
     *
     * @return double value generated by the RNG
     */
    @Override
    public Double execute() {
        return continuousUniformGenerator.nextValue();
    }

    /**
     * Get the distribution name
     *
     * @return string distribution name
     */
    @Override
    public String getDistributionName() {
        return NAME;
    }

}
