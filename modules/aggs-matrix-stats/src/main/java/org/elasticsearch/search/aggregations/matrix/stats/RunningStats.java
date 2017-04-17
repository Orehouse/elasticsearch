/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.elasticsearch.search.aggregations.matrix.stats;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Writeable;

import java.io.IOException;
import java.util.*;

/**
 * Descriptive stats gathered per shard. Coordinating node computes final correlation and covariance stats
 * based on these descriptive stats. This single pass, parallel approach is based on:
 *
 * http://prod.sandia.gov/techlib/access-control.cgi/2008/086212.pdf
 */
public class RunningStats implements Writeable, Cloneable {
    /** count of observations (same number of observations per field) */
    protected long docCount = 0;
    /** per field sum of observations */
    protected HashMap<String, Double> fieldSum;
    /** counts */
    protected HashMap<String, Long> counts;
    /** mean values (first moment) */
    protected HashMap<String, Double> means;
    /** variance values (second moment) */
    protected HashMap<String, Double> variances;
    /** skewness values (third moment) */
    protected HashMap<String, Double> skewness;
    /** kurtosis values (fourth moment) */
    protected HashMap<String, Double> kurtosis;
    /** covariance values */
    protected HashMap<String, HashMap<String, Double>> covariances;
    /** 90% quantile (percentile) **/
    protected double quantile90 = 0;

    private HashMap<String, ArrayList<Double>> lastObservations;
    private HashMap<String, ArrayList<Double>> markerHeights;
    private HashMap<String, ArrayList<Integer>> markerPositions;
    private HashMap<String, ArrayList<Double>> desiredMarkerPositions;
    private HashMap<String, ArrayList<Double>> desiredMarkerDeltas;


    public RunningStats() {
        init();
    }

    public RunningStats(final String[] fieldNames, final double[] fieldVals) {
        if (fieldVals != null && fieldVals.length > 0) {
            init();
            this.add(fieldNames, fieldVals);
        }
    }

    private void init() {
        counts = new HashMap<>();
        fieldSum = new HashMap<>();
        means = new HashMap<>();
        skewness = new HashMap<>();
        kurtosis = new HashMap<>();
        covariances = new HashMap<>();
        variances = new HashMap<>();
        lastObservations = new HashMap<>();
        markerHeights = new HashMap<>();
        markerPositions = new HashMap<>();
        desiredMarkerPositions = new HashMap<>();
        desiredMarkerDeltas = new HashMap<>();
    }

    /** Ctor to create an instance of running statistics */
    @SuppressWarnings("unchecked")
    public RunningStats(StreamInput in) throws IOException {
        this();
        // read doc count
        docCount = (Long)in.readGenericValue();
        // read fieldSum
        fieldSum = (HashMap<String, Double>)in.readGenericValue();
        // counts
        counts = (HashMap<String, Long>)in.readGenericValue();
        // means
        means = (HashMap<String, Double>)in.readGenericValue();
        // variances
        variances = (HashMap<String, Double>)in.readGenericValue();
        // skewness
        skewness = (HashMap<String, Double>)in.readGenericValue();
        // kurtosis
        kurtosis = (HashMap<String, Double>)in.readGenericValue();
        // read covariances
        covariances = (HashMap<String, HashMap<String, Double>>)in.readGenericValue();
        //90 quantile
        quantile90 = (Double)in.readGenericValue();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        // marshall doc count
        out.writeGenericValue(docCount);
        // marshall fieldSum
        out.writeGenericValue(fieldSum);
        // counts
        out.writeGenericValue(counts);
        // mean
        out.writeGenericValue(means);
        // variances
        out.writeGenericValue(variances);
        // skewness
        out.writeGenericValue(skewness);
        // kurtosis
        out.writeGenericValue(kurtosis);
        // covariances
        out.writeGenericValue(covariances);
        //90 quantile
        out.writeGenericValue(quantile90);
    }

    /** updates running statistics with a documents field values **/
    public void add(final String[] fieldNames, final double[] fieldVals) {
        if (fieldNames == null) {
            throw new IllegalArgumentException("Cannot add statistics without field names.");
        } else if (fieldVals == null) {
            throw new IllegalArgumentException("Cannot add statistics without field values.");
        } else if (fieldNames.length != fieldVals.length) {
            throw new IllegalArgumentException("Number of field values do not match number of field names.");
        }

        // update total, mean, and variance
        ++docCount;
        String fieldName;
        double fieldValue;
        double m1, m2, m3, m4;  // moments
        double d, dn, dn2, t1;
        final HashMap<String, Double> deltas = new HashMap<>();
        for (int i = 0; i < fieldNames.length; ++i) {
            fieldName = fieldNames[i];
            fieldValue = fieldVals[i];

            // update counts
            counts.put(fieldName, 1 + (counts.containsKey(fieldName) ? counts.get(fieldName) : 0));
            // update running sum
            fieldSum.put(fieldName, fieldValue + (fieldSum.containsKey(fieldName) ? fieldSum.get(fieldName) : 0));
            // update running deltas
            deltas.put(fieldName, fieldValue * docCount - fieldSum.get(fieldName));

            // update running mean, variance, skewness, kurtosis
            if (means.containsKey(fieldName) == true) {
                // update running means
                m1 = means.get(fieldName);
                d = fieldValue - m1;
                means.put(fieldName, m1 + d / docCount);
                // update running variances
                dn = d / docCount;
                t1 = d * dn * (docCount - 1);
                m2 = variances.get(fieldName);
                variances.put(fieldName, m2 + t1);
                m3 = skewness.get(fieldName);
                skewness.put(fieldName, m3 + (t1 * dn * (docCount - 2D) - 3D * dn * m2));
                dn2 = dn * dn;
                m4 = t1 * dn2 * (docCount * docCount - 3D * docCount + 3D) + 6D * dn2 * m2 - 4D * dn * m3;
                kurtosis.put(fieldName, kurtosis.get(fieldName) + m4);
            } else {
                means.put(fieldName, fieldValue);
                variances.put(fieldName, 0.0);
                skewness.put(fieldName, 0.0);
                kurtosis.put(fieldName, 0.0);
            }
        }

        this.updateCovariance(fieldNames, deltas);
        this.updateQuantiles(fieldNames, fieldVals);
    }

    private void updateQuantiles(final String[] fieldNames, final double[] fieldVals) {
        if (docCount == 0) {
            for (String fieldName : fieldNames) {
                this.lastObservations.put(fieldName, new ArrayList<>());

                this.markerHeights.put(fieldName, new ArrayList<>());
                this.markerHeights.get(fieldName).add(0, 1.0);
                this.markerHeights.get(fieldName).add(1, 2.0);
                this.markerHeights.get(fieldName).add(2, 3.0);
                this.markerHeights.get(fieldName).add(3, 4.0);
                this.markerHeights.get(fieldName).add(4, 5.0);

                this.markerPositions.put(fieldName, new ArrayList<>());
                this.markerPositions.get(fieldName).add(0, 1);
                this.markerPositions.get(fieldName).add(1, 2);
                this.markerPositions.get(fieldName).add(2, 3);
                this.markerPositions.get(fieldName).add(3, 4);
                this.markerPositions.get(fieldName).add(4, 5);

                this.desiredMarkerPositions.put(fieldName, new ArrayList<>());
                this.desiredMarkerPositions.get(fieldName).add(0, 1.0);
                double quantile = 0.9;
                this.desiredMarkerPositions.get(fieldName).add(1, 1 + 2 * quantile);
                this.desiredMarkerPositions.get(fieldName).add(2, 1 + 4 * quantile);
                this.desiredMarkerPositions.get(fieldName).add(3, 3 + 2 * quantile);
                this.desiredMarkerPositions.get(fieldName).add(0, 5.0);

                this.desiredMarkerDeltas.put(fieldName, new ArrayList<>());
                this.desiredMarkerDeltas.get(fieldName).add(0, 0.0);
                this.desiredMarkerDeltas.get(fieldName).add(0, quantile / 2);
                this.desiredMarkerDeltas.get(fieldName).add(0, quantile);
                this.desiredMarkerDeltas.get(fieldName).add(0, (1 + quantile) / 2);
                this.desiredMarkerDeltas.get(fieldName).add(0, 1.0);
            }
        }

        if (docCount < 5) {
            for (int i = 0; i < fieldNames.length; i++) {
                String fieldName = fieldNames[i];
                Double fieldValue = fieldVals[i];
                this.lastObservations.get(fieldNames[i]).add((int)docCount, fieldValue);
            }
        } else {
            for (int i = 0; i < fieldNames.length; i++) {
                int k;
                String fieldName = fieldNames[i];
                Double fieldValue = fieldVals[i];
                if (fieldValue < markerHeights.get(fieldName).get(0)) {
                    markerHeights.get(fieldName).set(0, fieldValue);
                    k = 0;
                } else if (fieldValue < markerHeights.get(fieldName).get(1)) {
                    k = 1;
                } else if (fieldValue < markerHeights.get(fieldName).get(2)) {
                    k = 2;
                } else if (fieldValue < markerHeights.get(fieldName).get(3)) {
                    k = 3;
                } else if (fieldValue <= markerHeights.get(fieldName).get(4)) {
                    k = 4;
                } else {
                    markerHeights.get(fieldName).set(4, fieldValue);
                    k = 5;
                }

                for (int j = k; j < 4; j++) {
                    this.markerPositions.get(fieldName).set(j, this.markerPositions.get(fieldName).get(j) + 1);
                }
                for (int j = 0; j < 4; j++) {
                    this.desiredMarkerPositions.get(fieldName).set(j, this.desiredMarkerPositions.get(fieldName).get(j) + this.desiredMarkerDeltas.get(fieldName).get(j));
                }
                double[] d = new double[5];
                for (int j = 1; j < 3; j++) {
                    ArrayList<Integer> n = this.markerPositions.get(fieldName);
                    ArrayList<Double> q = this.desiredMarkerPositions.get(fieldName);
                    d[j] = q.get(j) - n.get(j);
                    if (d[j] >= 1 && n.get(j + 1) - n.get(j) > 1
                        || d[j] <= -1 && n.get(j - 1) - n.get(j) < -1) {
                        d[j] = d[j] > 0 ? 1 : d[j] == 0 ? 0 : -1;
                        this.markerHeights.get(fieldName).set(j,
                            this.markerHeights.get(fieldName).get(j) + d[j]
                                / (n.get(j + 1) - n.get(j - 1))
                                * ((n.get(j) - n.get(j - 1) + d[j]) * (q.get(j + 1) - q.get(j)) / (n.get(j + 1) - n.get(j))
                                + (n.get(j + 1) - n.get(j) - d[j]) * (q.get(j) - q.get(j - 1)) / (n.get(j) - n.get(j - 1))));
                        n.set(j, (int) Math.round(n.get(j) + d[j]));
                    }
                }

                quantile90 = this.markerHeights.get(fieldName).get(2);
            }
        }
    }

    /** Update covariance matrix */
    private void updateCovariance(final String[] fieldNames, final Map<String, Double> deltas) {
        // deep copy of hash keys (field names)
        ArrayList<String> cFieldNames = new ArrayList<>(Arrays.asList(fieldNames));
        String fieldName;
        double dR, newVal;
        for (int i = 0; i < fieldNames.length; ++i) {
            fieldName = fieldNames[i];
            cFieldNames.remove(fieldName);
            // update running covariances
            dR = deltas.get(fieldName);
            HashMap<String, Double> cFieldVals = (covariances.get(fieldName) != null) ? covariances.get(fieldName) : new HashMap<>();
            for (String cFieldName : cFieldNames) {
                if (cFieldVals.containsKey(cFieldName) == true) {
                    newVal = cFieldVals.get(cFieldName) + 1.0 / (docCount * (docCount - 1.0)) * dR * deltas.get(cFieldName);
                    cFieldVals.put(cFieldName, newVal);
                } else {
                    cFieldVals.put(cFieldName, 0.0);
                }
            }
            if (cFieldVals.size() > 0) {
                covariances.put(fieldName, cFieldVals);
            }
        }
    }

    /**
     * Merges the descriptive statistics of a second data set (e.g., per shard)
     *
     * running computations taken from: http://prod.sandia.gov/techlib/access-control.cgi/2008/086212.pdf
     **/
    public void merge(final RunningStats other) {
        if (other == null) {
            return;
        } else if (this.docCount == 0) {
            for (Map.Entry<String, Double> fs : other.means.entrySet()) {
                final String fieldName = fs.getKey();
                this.means.put(fieldName, fs.getValue().doubleValue());
                this.counts.put(fieldName, other.counts.get(fieldName).longValue());
                this.fieldSum.put(fieldName, other.fieldSum.get(fieldName).doubleValue());
                this.variances.put(fieldName, other.variances.get(fieldName).doubleValue());
                this.skewness.put(fieldName , other.skewness.get(fieldName).doubleValue());
                this.kurtosis.put(fieldName, other.kurtosis.get(fieldName).doubleValue());
                if (other.covariances.containsKey(fieldName) == true) {
                    this.covariances.put(fieldName, other.covariances.get(fieldName));
                }
                this.docCount = other.docCount;
                this.quantile90 = other.quantile90;
            }
            return;
        }
        final double nA = docCount;
        final double nB = other.docCount;
        // merge count
        docCount += other.docCount;

        final HashMap<String, Double> deltas = new HashMap<>();
        double meanA, varA, skewA, kurtA, meanB, varB, skewB, kurtB;
        double d, d2, d3, d4, n2, nA2, nB2;
        double newSkew, nk;
        // across fields
        for (Map.Entry<String, Double> fs : other.means.entrySet()) {
            final String fieldName = fs.getKey();
            meanA = means.get(fieldName);
            varA = variances.get(fieldName);
            skewA = skewness.get(fieldName);
            kurtA = kurtosis.get(fieldName);
            meanB = other.means.get(fieldName);
            varB = other.variances.get(fieldName);
            skewB = other.skewness.get(fieldName);
            kurtB = other.kurtosis.get(fieldName);

            // merge counts of two sets
            counts.put(fieldName, counts.get(fieldName) + other.counts.get(fieldName));

            // merge means of two sets
            means.put(fieldName, (nA * means.get(fieldName) + nB * other.means.get(fieldName)) / (nA + nB));

            // merge deltas
            deltas.put(fieldName, other.fieldSum.get(fieldName) / nB - fieldSum.get(fieldName) / nA);

            // merge totals
            fieldSum.put(fieldName, fieldSum.get(fieldName) + other.fieldSum.get(fieldName));

            // merge variances, skewness, and kurtosis of two sets
            d = meanB - meanA;          // delta mean
            d2 = d * d;                 // delta mean squared
            d3 = d * d2;                // delta mean cubed
            d4 = d2 * d2;               // delta mean 4th power
            n2 = docCount * docCount;   // num samples squared
            nA2 = nA * nA;              // doc A num samples squared
            nB2 = nB * nB;              // doc B num samples squared
            // variance
            variances.put(fieldName, varA + varB + d2 * nA * other.docCount / docCount);
            // skeewness
            newSkew = skewA + skewB + d3 * nA * nB * (nA - nB) / n2;
            skewness.put(fieldName, newSkew + 3D * d * (nA * varB - nB * varA) / docCount);
            // kurtosis
            nk = kurtA + kurtB + d4 * nA * nB * (nA2 - nA * nB + nB2) / (n2 * docCount);
            kurtosis.put(fieldName, nk + 6D * d2 * (nA2 * varB + nB2 * varA) / n2 + 4D * d * (nA * skewB - nB * skewA) / docCount);
            this.quantile90 = (this.quantile90 * this.docCount + other.quantile90 * other.docCount)
                / (this.docCount + other.docCount);
        }

        this.mergeCovariance(other, deltas);
    }

    /** Merges two covariance matrices */
    private void mergeCovariance(final RunningStats other, final Map<String, Double> deltas) {
        final double countA = docCount - other.docCount;
        double f, dR, newVal;
        for (Map.Entry<String, Double> fs : other.means.entrySet()) {
            final String fieldName = fs.getKey();
            // merge covariances of two sets
            f = countA * other.docCount / this.docCount;
            dR = deltas.get(fieldName);
            // merge covariances
            if (covariances.containsKey(fieldName)) {
                HashMap<String, Double> cFieldVals = covariances.get(fieldName);
                for (String cFieldName : cFieldVals.keySet()) {
                    newVal = cFieldVals.get(cFieldName);
                    if (other.covariances.containsKey(fieldName) && other.covariances.get(fieldName).containsKey(cFieldName)) {
                        newVal += other.covariances.get(fieldName).get(cFieldName) + f * dR * deltas.get(cFieldName);
                    } else {
                        newVal += other.covariances.get(cFieldName).get(fieldName) + f * dR * deltas.get(cFieldName);
                    }
                    cFieldVals.put(cFieldName, newVal);
                }
                covariances.put(fieldName, cFieldVals);
            }
        }
    }

    @Override
    public RunningStats clone() {
        try {
            return (RunningStats) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new ElasticsearchException("Error trying to create a copy of RunningStats");
        }
    }
}
