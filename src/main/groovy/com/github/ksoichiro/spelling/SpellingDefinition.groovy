package com.github.ksoichiro.spelling

class SpellingDefinition {
    List<SpellingRule> rules

    SpellingDefinition() {
        rules = []
    }

    void rules(Closure configureClosure) {
        configureClosure.delegate = this
        configureClosure()
    }

    void define(Map configuration) {
        rules.add(new SpellingRule(configuration))
    }
}
