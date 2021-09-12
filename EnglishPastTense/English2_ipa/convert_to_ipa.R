require(tidyverse)

setwd('~/Researchers/HayesBruce/AlbrightHayes2003')

fverbs = c('CELEXFull.in', 'CELEXPrefixStrip.in')[2]
verbs_in = read_tsv(
    str_glue('English2/RuleBasedLearnerEnglishFiles/{fverbs}'),
    skip=9, locale = locale(encoding = 'ISO-8859-2'), col_names=c('lemma', 'past', 'celex_freq', 'lemma_orth', 'past_orth', 'past_type', 'notes'))

verbs_in %>%
    mutate(lemma_ipa = gsub('(.)', '\\1 ', lemma)) %>%
    mutate(past_ipa = gsub('(.)', '\\1 ', past)) -> verbs_in

subs = c('C'='tʃ', 'J'='dʒ', 'D'='ð', 'S'='ʃ', 'T'='θ', 'Z'='ʒ', 'N'='ŋ', 'r'='ɹ', 'e'='eɪ', 'U'='ʊ', 'Q'='æ', 'I'='ɪ', 'E'='ɛ', ''='ɔ', 'o'='oʊ', 'Ť'='ə', 'Ă'='ʌ', 'Ő'='ɚ', 'Y'='aɪ', 'W'='aʊ', 'O'='ɔɪ', 'Č'="ˈ")
print(subs)

verbs_out = verbs_in
for (i in 1:length(subs)) {
    x = names(subs)[i]
    y = drop(subs[i])
    verbs_out$lemma_ipa = gsub(x, y, verbs_out$lemma_ipa)
    verbs_out$past_ipa = gsub(x, y, verbs_out$past_ipa)
}

verbs_out$lemma_ipa = gsub("ˈ ", "ˈ", verbs_out$lemma_ipa)
verbs_out$past_ipa = gsub("ˈ ", "ˈ", verbs_out$past_ipa)

verbs_out %>%
    select(lemma_ipa, past_ipa, celex_freq, lemma_orth, past_orth, past_type, notes, lemma_orig=lemma, past_orig=past) -> verbs_out

write_tsv(verbs_out, str_glue('English2_ipa/{fverbs}'))
