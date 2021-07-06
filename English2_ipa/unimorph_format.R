require(tidyverse)

setwd('~/Researchers/HayesBruce/AlbrightHayes2003/English2_ipa')

for (fdat_in in c('CELEXFull.in', 'CELEXPrefixStrip.in')) {

dat_in = read_tsv(fdat_in)

dat_in %>%
    mutate(lemma_ipa = gsub("ˈ", '', lemma_ipa)) %>%
    mutate(past_ipa = gsub("ˈ", '', past_ipa)) %>%
    mutate(morphosyn='V;PST;') %>%
    select(lemma_ipa, past_ipa, morphosyn, lemma_orth, past_orth) %>%
    #drop_na() %>%
    identity() -> 
    dat_out

fdat_out = str_glue('{fdat_in}.unimorph')
#write_tsv(dat_out, fdat_out, col_names=FALSE)
}