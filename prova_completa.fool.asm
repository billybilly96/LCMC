push 0
lhp
push method0
lhp
sw
push 1 
lhp
add
shp
push method1
lhp
sw
push 1 
lhp
add
shp
push method2
lhp
sw
push 1 
lhp
add
shp
push method3
lhp
sw
push 1 
lhp
add
shp
push method4
lhp
sw
push 1 
lhp
add
shp
push method5
lhp
sw
push 1 
lhp
add
shp
lhp
push method0
lhp
sw
push 1 
lhp
add
shp
push method1
lhp
sw
push 1 
lhp
add
shp
push method2
lhp
sw
push 1 
lhp
add
shp
push method3
lhp
sw
push 1 
lhp
add
shp
push method4
lhp
sw
push 1 
lhp
add
shp
push method5
lhp
sw
push 1 
lhp
add
shp
push method6
lhp
sw
push 1 
lhp
add
shp
lhp
push method0
lhp
sw
push 1 
lhp
add
shp
push method1
lhp
sw
push 1 
lhp
add
shp
push method2
lhp
sw
push 1 
lhp
add
shp
push method7
lhp
sw
push 1 
lhp
add
shp
push method4
lhp
sw
push 1 
lhp
add
shp
push method5
lhp
sw
push 1 
lhp
add
shp
lfp
push function0
lfp
push function1
push 12
push 4
push 6
push 1
lhp
sw
push 1
lhp
add
shp
lhp
sw
push 1
lhp
add
shp
lhp
sw
push 1
lhp
add
shp
lhp
sw
push 1
lhp
add
shp
push 9997
lw
lhp
sw
lhp
push 1
lhp
add
shp
push 2
push 1
push 3
lhp
sw
push 1
lhp
add
shp
lhp
sw
push 1
lhp
add
shp
lhp
sw
push 1
lhp
add
shp
push 9996
lw
lhp
sw
lhp
push 1
lhp
add
shp
lfp
push -5
lfp
add
lw
push -6
lfp
add
lw
push -9
lfp
add
lw
push -9
lfp
add
lw
lw
push 4
add
lw
js
lfp
push -9
lfp
add
lw
push -9
lfp
add
lw
lw
push 6
add
lw
js
lfp
push -10
lfp
add
lw
push -10
lfp
add
lw
lw
push 3
add
lw
js
mult
add

push 0
beq label8
push 1
b label9
label8: 
push 0
label9: 
push 1
beq label6
push -10
lfp
add
lw
b label7
label6: 
push -9
lfp
add
lw
label7: 
lfp
push -7
lfp
add
lw
push -8
lfp
add
lw
push -11
lfp
add
lw
push -11
lfp
add
lw
lw
push 5
add
lw
js
print
halt

method0:
cfp
lra
push -1
lfp
lw
add
lw
srv
sra
pop
sfp
lrv
lra
js

method1:
cfp
lra
push -2
lfp
lw
add
lw
srv
sra
pop
sfp
lrv
lra
js

method2:
cfp
lra
push -3
lfp
lw
add
lw
srv
sra
pop
sfp
lrv
lra
js

method3:
cfp
lra
push -3
lfp
lw
add
lw
push -1
lfp
lw
add
lw
push -2
lfp
lw
add
lw
div
bleq label0
push 0
b label1
label0: 
push 1
label1: 
srv
sra
pop
sfp
lrv
lra
js

method4:
cfp
lra
lfp
lfp
lfp
lw
push 2
lfp
lw
lw
add
lw
js
lfp
lfp
lw
push 1
lfp
lw
lw
add
lw
js
lfp
lfp
lw
push 0
lfp
lw
lw
add
lw
js
push 2
lfp
add
lw 
push 1
lfp
add
lw
js
srv
sra
pop
pop
pop
sfp
lrv
lra
js

method5:
cfp
lra
lfp
lfp
lfp
lw
push 2
lfp
lw
lw
add
lw
js
lfp
lfp
lw
push 1
lfp
lw
lw
add
lw
js
lfp
lfp
lw
push 0
lfp
lw
lw
add
lw
js
push 2
lfp
add
lw 
push 1
lfp
add
lw
js
srv
sra
pop
pop
pop
sfp
lrv
lra
js

method6:
cfp
lra
push 1
push -4
lfp
lw
add
lw
sub
srv
sra
pop
sfp
lrv
lra
js

method7:
cfp
lra
push -1
lfp
lw
add
lw
push -2
lfp
lw
add
lw
div
push -3
lfp
lw
add
lw
bleq label2
push 0
b label3
label2: 
push 1
label3: 
srv
sra
pop
sfp
lrv
lra
js

function0:
cfp
lra
push 3
lfp
add
lw
push 1
lfp
add
lw
push 2
lfp
add
lw
div
bleq label4
push 0
b label5
label4: 
push 1
label5: 
srv
sra
pop
pop
pop
pop
sfp
lrv
lra
js

function1:
cfp
lra
push 1
lfp
add
lw
push 2
lfp
add
lw
sub
push 3
lfp
add
lw
sub
srv
sra
pop
pop
pop
pop
sfp
lrv
lra
js
