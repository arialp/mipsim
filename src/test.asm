addi $t1, $zero, 20
addi $t0, $zero, 5
test1:
add $t0, $t0, $t0
bne $t0, $t1, test1
add $s0, $t0, $zero
jal test2
add $s5, $t1, $t0
j test3
test2:
sll $s1, $t1, 2
sub $s2, $s1, $t0
sll $s3, $s2, 3
srl $s4, $s3, 1
jr $ra
test3:
sw $s0, 0($sp)
sw $s1, 4($sp)
sw $s2, 8($sp)
sw $s3, 12($sp)
sw $s4, 16($sp)
sw $s5, 20($sp)
lw $t0, 12($sp)
lw $t1, 4($sp)
add $s6, $t1, $t0
sw $s6, 24($sp)