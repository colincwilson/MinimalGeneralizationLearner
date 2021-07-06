# Replace ASCII characters with single unicode characters 
# closer to IPA in .fea, .in files

require(tidyverse)

in_dir = '~/Library/Java/MinimalGeneralizationLearner/English2/'
out_dir = '~/Library/Java/MinimalGeneralizationLearner/English2_unicode/'

enc = 'ISO-8859-2' # 'latin1'
ftrs = read_tsv(
    str_glue(in_dir, 'CELEXFull.fea'), # 'CELEXPrefixStrip.fea'
    locale = locale(encoding = enc))
verbs = read_tsv(
    str_glue(in_dir, 'CELEXFull.in'), # 'CELEXPrefixStrip.in'
    skip=9, locale = locale(encoding = enc), col_names=c('lemma', 'past', 'celex_freq', 'lemma_orth', 'past_orth', 'past_type', 'notes'))

unicode_subs = c('D'='ð', 'S'='ʃ', 'T'='θ', 'Z'='ʒ', 'N'='ŋ', 'r'='ɹ', 
'I'='ɪ', 'E'='ɛ', 'Q'='æ', 'U'='ʊ', ''='ɔ', 'Ť'='ə', 'Ă'='ʌ', 'Ő'='ɚ', 'Č'="ˈ", '\u0081'='X')
# what is X?

# Map extended ASCII to UTF-8
for (i in 1:length(unicode_subs)) {
    s = names(unicode_subs)[i]
    r = drop(unicode_subs[i])
    ftrs$Seg. = gsub(s, r, ftrs$Seg.)
    verbs$lemma = gsub(s, r, verbs$lemma)
    verbs$past = gsub(s, r, verbs$past)
}

# Remove stress
#verbs$lemma = gsub("ˈ", '', verbs$lemma)
#verbs$past = gsub("ˈ", '', verbs$past)

write_tsv(
    ftrs,
    str_glue(out_dir, 'CELEXFull_unicode.fea'),
    na = '')
# Feature file is in correct format

write_tsv(
    verbs,
    str_glue(out_dir, 'CELEXFull_unicode.in'),
    na = '')
# Lexicon file needs some further fixing:
# - Replace column names with standard header
# - Fix section headers ("Test forms:", "Illicit sequences:")
# - Remove extra tabs at line ends