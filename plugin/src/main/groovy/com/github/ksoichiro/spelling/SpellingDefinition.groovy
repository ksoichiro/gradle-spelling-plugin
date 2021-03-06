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

    void configure(NodeList nodeList) {
        if (nodeList.rules) {
            nodeList.rules.rule.each { Node rule ->
                def spellingRule = new SpellingRule()
                spellingRule.configure(rule)
                rules.add(spellingRule)
            }
        }
    }
}
