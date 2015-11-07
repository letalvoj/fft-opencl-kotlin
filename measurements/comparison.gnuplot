set logscale y
set logscale x

set format y "10^{%L}"
set format x "10^{%L}"

set xlabel 'Sample size'
set ylabel 'Duration [ms]'
set title 'Comparison of FFT runtime on CPU vs. GPU'

plot 'comparison.csv' using 1:2 title 'Time on CPU', 'comparison.csv' using 1:3 title 'Time on GPU'