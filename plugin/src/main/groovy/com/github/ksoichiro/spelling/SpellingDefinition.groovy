package com.github.ksoichiro.spelling

class SpellingDefinition {
    List<SpellingRule> rules

    SpellingDefinition() {
        this.rules = []
    }

    void rules(Closure configureClosure) {
        configureClosure.delegate = this
        configureClosure()
    }

    void define(Map configuration) {
        rules.add(new SpellingRule(configuration))
    }

    def methodMissing(String name, args) {
        this."$name" = args[0]
    }
}
