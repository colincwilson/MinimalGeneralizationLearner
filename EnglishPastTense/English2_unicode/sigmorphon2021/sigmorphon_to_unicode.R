# Convert sigmorphon data to same format as unicode .in files

require(tidyverse)

in_dir = '~/Languages/UniMorph/sigmorphon2021/2021Task0/part2/'
out_dir='~/Library/Java/MinimalGeneralization/English2_unicode/sigmorphon2021/'

split = c('dev', 'tst')[2]

wugs = read_tsv(
    str_glue(in_dir, 'eng.judgements.{split}'), # 'eng.judgements.tst'
    col_names=c('lemma', 'past', 'morphosyn', 'human_rating'))

# British English IPA -> MinGenLearner unicode
# Notes
# - /a/ corresponds to American /æ/
# - /ɑː/ corresponds to American /æ/ or /aɹ/ (with written <r>) or /a/ (rarely)
subs = c('t ʃ'='C', 'd ʒ'='J', 'r'='ɹ', 
'iː'='i', 'eɪ'='e', 'a'='æ', 'ɑː'='æ', 
'uː'='u', 'əʊ'='o', 'oʊ'='o', 'ɔː'='ɔ', 
'aɪ'='Y', 'aʊ'='W', 'ɔɪ'='O', 
'ɡ'='g')
subs = subs[order(-nchar(names(subs)))] # Longer apply earlier

wugs$lemma_orig = wugs$lemma
wugs$past_orig = wugs$past
for (i in 1:length(subs)) {
    s = names(subs)[i]
    r = drop(subs[i])
    wugs$lemma = gsub(s, r, wugs$lemma)
    wugs$past = gsub(s, r, wugs$past)
}
wugs$lemma = gsub(' ', '', wugs$lemma)
wugs$past = gsub(' ', '', wugs$past)

write_tsv(
    wugs,
    str_glue(out_dir, 'sigmorphon2021_english_wug_{split}.tsv'))
