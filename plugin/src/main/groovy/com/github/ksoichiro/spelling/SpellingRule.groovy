package com.github.ksoichiro.spelling

class SpellingRule {
    String forbidden
    String recommended

    void configure(Node node) {
        forbidden = node.@forbidden
        recommended = node.@recommended
    }
}
