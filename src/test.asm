addi $t1, $zero, 20      # $t1 = 20
addi $t0, $zero, 5       # $t0 = 5
test1:                   # Label: test1
add $t0, $t0, $t0        # $t0 = $t0 + $t0 (çiftleniyor)
bne $t0, $t1, test1      # Eğer $t0 != $t1, test1'e geri dön
add $s0, $t0, $zero      # $s0 = $t0
jal test2                # test2 alt programına git, $ra = PC + 4
add $s5, $t1, $t0        # $s5 = $t1 + $t0
j test3                  # test3'e dallan
test2:                   # Label: test2
sll $s1, $t1, 2          # $s1 = $t1 << 2 (4 ile çarp)
sub $s2, $s1, $t0        # $s2 = $s1 - $t0
sll $s3, $s2, 3          # $s3 = $s2 << 3 (8 ile çarp)
srl $s4, $s3, 1          # $s4 = $s3 >> 1 (2'ye böl)
jr $ra                   # $ra'ya dön
test3:                   # Label: test3
sw $s0, 0($sp)           # Stack'te $s0'ı 0($sp)'ye kaydet
sw $s1, 4($sp)           # Stack'te $s1'i 4($sp)'ye kaydet
sw $s2, 8($sp)           # Stack'te $s2'yi 8($sp)'ye kaydet
sw $s3, 12($sp)          # Stack'te $s3'ü 12($sp)'ye kaydet
sw $s4, 16($sp)          # Stack'te $s4'ü 16($sp)'ye kaydet
sw $s5, 20($sp)          # Stack'te $s5'i 20($sp)'ye kaydet
lw $t0, 12($sp)          # Stack'ten $t0'u 12($sp)'den yükle
lw $t1, 4($sp)           # Stack'ten $t1'i 4($sp)'den yükle
add $s6, $t1, $t0        # $s6 = $t1 + $t0
sw $s6, 24($sp)          # Stack'te $s6'yı 24($sp)'ye kaydet
