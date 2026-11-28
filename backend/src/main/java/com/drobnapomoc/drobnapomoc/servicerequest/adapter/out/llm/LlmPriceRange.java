package com.drobnapomoc.drobnapomoc.servicerequest.adapter.out.llm;

/**
 * The answer the model must return, in whole złote (easier for the model than grosze).
 */
record LlmPriceRange(int minPln, int maxPln) {
}
