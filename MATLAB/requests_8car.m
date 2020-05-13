csvState = readtable('CarData_CarState_0x6a40.csv');
carState = table2array(csvState(1:end, 2))/1000000;
carState1 = cat(2, carState, table2array(csvState(1:end, 12)));

csvState = readtable('CarData_CarState_0x6743.csv');
carState = table2array(csvState(1:end, 2))/1000000;
carState2 = cat(2, carState, table2array(csvState(1:end, 12)));

csvState = readtable('CarData_CarState_0x673b.csv');
carState = table2array(csvState(1:end, 2))/1000000;
carState3 = cat(2, carState, table2array(csvState(1:end, 12)));

csvState = readtable('CarData_CarState_0x6a1a.csv');
carState = table2array(csvState(1:end, 2))/1000000;
carState4 = cat(2, carState, table2array(csvState(1:end, 12)));

csvState = readtable('CarData_CarState_0x5555.csv');
carState = table2array(csvState(1:end, 2))/1000000;
carState5 = cat(2, carState, table2array(csvState(1:end, 12)));

csvState = readtable('CarData_CarState_0x6666.csv');
carState = table2array(csvState(1:end, 2))/1000000;
carState6 = cat(2, carState, table2array(csvState(1:end, 12)));

csvState = readtable('CarData_CarState_0x7777.csv');
carState = table2array(csvState(1:end, 2))/1000000;
carState7 = cat(2, carState, table2array(csvState(1:end, 12)));

csvState = readtable('CarData_CarState_0x8888.csv');
carState = table2array(csvState(1:end, 2))/1000000;
carState8 = cat(2, carState, table2array(csvState(1:end, 12)));

csv_queue = readtable('IntersectionQueue.csv');

csvTracker = readtable('0x6a40_CarTracker.csv');
tracker = table2array(csvTracker(1:end, 2))/1000000;
res = strcmp(table2array(csvTracker(1:end, 10)), 'true');
tracker1 = cat(2, tracker, res);

csvTracker = readtable('0x6743_CarTracker.csv');
tracker = table2array(csvTracker(1:end, 2))/1000000;
res = strcmp(table2array(csvTracker(1:end, 10)), 'true');
tracker2 = cat(2, tracker, res);

csvTracker = readtable('0x673b_CarTracker.csv');
tracker = table2array(csvTracker(1:end, 2))/1000000;
res = strcmp(table2array(csvTracker(1:end, 10)), 'true');
tracker3 = cat(2, tracker, res);

csvTracker = readtable('0x6a1a_CarTracker.csv');
tracker = table2array(csvTracker(1:end, 2))/1000000;
res = strcmp(table2array(csvTracker(1:end, 10)), 'true');
tracker4 = cat(2, tracker, res);

csvTracker = readtable('0x5555_CarTracker.csv');
tracker = table2array(csvTracker(1:end, 2))/1000000;
res = strcmp(table2array(csvTracker(1:end, 10)), 'true');
tracker5 = cat(2, tracker, res);

csvTracker = readtable('0x6666_CarTracker.csv');
tracker = table2array(csvTracker(1:end, 2))/1000000;
res = strcmp(table2array(csvTracker(1:end, 10)), 'true');
tracker6 = cat(2, tracker, res);

csvTracker = readtable('0x7777_CarTracker.csv');
tracker = table2array(csvTracker(1:end, 2))/1000000;
res = strcmp(table2array(csvTracker(1:end, 10)), 'true');
tracker7 = cat(2, tracker, res);

csvTracker = readtable('0x8888_CarTracker.csv');
tracker = table2array(csvTracker(1:end, 2))/1000000;
res = strcmp(table2array(csvTracker(1:end, 10)), 'true');
tracker8 = cat(2, tracker, res);



time = table2array(csv_queue(1:end,2))/1000000;
ids = char(table2array(csv_queue(1:end,3)));
ids_val = hex2dec(ids(:,3:end));
reservation = table2array(csv_queue(1:end,4));
reservation = (reservation *2)-1;
throttle = table2array(csv_queue(1:end,9));
speed = table2array(csv_queue(1:end,8));

entries = cat(2, time, ids_val, reservation, throttle, speed);

mints = [carState(1,1), carState2(1,1), carState3(1,1), carState4(1,1), carState5(1,1), carState6(1,1), carState7(1,1), carState8(1,1)];
mints = cat(2, mints, [tracker1(1,1), tracker2(1,1), tracker3(1,1), tracker4(1,1), tracker5(1,1), tracker6(1,1), tracker7(1,1), tracker8(1,1)]);
mints = cat(2, mints, entries(1,1));
minTime = min(mints);

carState1(:,1) = carState1(:,1) - minTime;
carState2(:,1) = carState2(:,1) - minTime;
carState3(:,1) = carState3(:,1) - minTime;
carState4(:,1) = carState4(:,1) - minTime;
carState5(:,1) = carState5(:,1) - minTime;
carState6(:,1) = carState6(:,1) - minTime;
carState7(:,1) = carState7(:,1) - minTime;
carState8(:,1) = carState8(:,1) - minTime;

tracker1(:,1) = tracker1(:,1) - minTime;
tracker2(:,1) = tracker2(:,1) - minTime;
tracker3(:,1) = tracker3(:,1) - minTime;
tracker4(:,1) = tracker4(:,1) - minTime;
tracker5(:,1) = tracker5(:,1) - minTime;
tracker6(:,1) = tracker6(:,1) - minTime;
tracker7(:,1) = tracker7(:,1) - minTime;
tracker8(:,1) = tracker8(:,1) - minTime;

entries(:,1) = entries(:,1) - minTime;

entry1 = entries(entries(:,2) == hex2dec('6a40'), :);
entry1(entry1(:,2) == 0) = -1; 
entry2 = entries(entries(:,2) == hex2dec('6743'), :);
entry2(entry2(:,2) == 0) = -1; 
entry3 = entries(entries(:,2) == hex2dec('673b'), :);
entry3(entry3(:,2) == 0) = -1; 
entry4 = entries(entries(:,2) == hex2dec('6a1a'), :);
entry4(entry4(:,2) == 0) = -1; 
entry5 = entries(entries(:,2) == hex2dec('5555'), :);
entry5(entry5(:,2) == 0) = -1; 
entry6 = entries(entries(:,2) == hex2dec('6666'), :);
entry6(entry6(:,2) == 0) = -1; 
entry7 = entries(entries(:,2) == hex2dec('7777'), :);
entry7(entry7(:,2) == 0) = -1; 
entry8 = entries(entries(:,2) == hex2dec('8888'), :);
entry8(entry8(:,2) == 0) = -1; 



figure();
xlabel('Time (ms)')
xrange =  [0 5000];
xlim(xrange);

hold on;

yyaxis left;
ylim([-1.1, 1.1]);
ylabel('Reservation Request (1 = Approved, -1 = Denied)');
x = entry1(:,1);
y = entry1(:,3);
stem(x,y,'--', 'Color', [0 0.4470 0.7410]);
x = entry2(:,1);
y = entry2(:,3);
stem(x,y, '--', 'Color', [0.8500 0.3250 0.0980]);
x = entry3(:,1);
y = entry3(:,3);
stem(x,y, '--', 'Color', [0.9290 0.6940 0.1250]);
x = entry4(:,1);
y = entry4(:,3);
stem(x,y, '--', 'Color', [0.4940 0.1840 0.5560]);
x = entry5(:,1);
y = entry5(:,3);
stem(x,y, '--', 'Color', [0.4660 0.6740 0.1880]);
x = entry6(:,1);
y = entry6(:,3);
stem(x,y, '--', 'Color', [0.3010 0.7450 0.9330]);
x = entry7(:,1);
y = entry7(:,3);
stem(x,y, '--', 'Color', [0.6350 0.0780 0.1840]);
x = entry8(:,1);
y = entry8(:,3);
stem(x,y, '--', 'Color', [0 0 0]);

yyaxis right;
ylabel('Car Speed (mm/s)');
x = carState1(:,1);
y = carState1(:,2);
stairs(x,y, '-o', 'Color', [0 0.4470 0.7410]);
x = carState2(:,1);
y = carState2(:,2);
stairs(x,y, '-o', 'Color', [0.8500 0.3250 0.0980]);
x = carState3(:,1);
y = carState3(:,2);
stairs(x,y, '-o', 'Color', [0.9290 0.6940 0.1250]);
x = carState4(:,1);
y = carState4(:,2);
stairs(x,y, '-o', 'Color', [0.4940 0.1840 0.5560]);
x = carState5(:,1);
y = carState5(:,2);
stairs(x,y, '-o', 'Color', [0.4660 0.6740 0.1880]);
x = carState6(:,1);
y = carState6(:,2);
stairs(x,y, '-o', 'Color', [0.3010 0.7450 0.9330]);
x = carState7(:,1);
y = carState7(:,2);
stairs(x,y, '-o', 'Color', [0.6350 0.0780 0.1840]);
x = carState8(:,1);
y = carState8(:,2);
stairs(x,y, '-o', 'Color', [0 0 0]);

legend('Car 1 (0x6a40)', 'Car 2 (0x6743)', 'Car 3 (0x673b)', 'Car 4 (0x6a1a)', 'Car 5 (0x5555)', 'Car 6 (0x6666)', 'Car 7 (0x7777)', 'Car 8 (0x8888)');

hold off;


figure();
xlabel('Time (ms)')
xrange =  [0 5000];
xlim(xrange);

hold on;

yyaxis left;
ylim([0 1.05]);
ylabel('Has approved Reservation (1 = true, 0 = false)');
x = tracker1(:,1);
y = tracker1(:,2);
stairs(x,y, '--o', 'Color', [0 0.4470 0.7410]);
x = tracker2(:,1);
y = tracker2(:,2);
stairs(x,y, '--o', 'Color', [0.8500 0.3250 0.0980]);
x = tracker3(:,1);
y = tracker3(:,2);
stairs(x,y,'--o',  'Color', [0.9290 0.6940 0.1250]);
x = tracker4(:,1);
y = tracker4(:,2);
stairs(x,y, '--o', 'Color', [0.4940 0.1840 0.5560]);
x = tracker5(:,1);
y = tracker5(:,2);
stairs(x,y, '--o', 'Color', [0.4660 0.6740 0.1880]);
x = tracker6(:,1);
y = tracker6(:,2);
stairs(x,y, '--o', 'Color', [0.3010 0.7450 0.9330]);
x = tracker7(:,1);
y = tracker7(:,2);
stairs(x,y,'--o',  'Color', [0.6350 0.0780 0.1840]);
x = tracker8(:,1);
y = tracker8(:,2);
stairs(x,y, '--o', 'Color', [0 0 0]);

yyaxis right;
ylabel('Car Speed (mm/s)');
x = carState1(:,1);
y = carState1(:,2);
stairs(x,y, '-o', 'Color', [0 0.4470 0.7410]);
x = carState2(:,1);
y = carState2(:,2);
stairs(x,y, '-o', 'Color', [0.8500 0.3250 0.0980]);
x = carState3(:,1);
y = carState3(:,2);
stairs(x,y, '-o', 'Color', [0.9290 0.6940 0.1250]);
x = carState4(:,1);
y = carState4(:,2);
stairs(x,y, '-o', 'Color', [0.4940 0.1840 0.5560]);
x = carState5(:,1);
y = carState5(:,2);
stairs(x,y, '-o', 'Color', [0.4660 0.6740 0.1880]);
x = carState6(:,1);
y = carState6(:,2);
stairs(x,y, '-o', 'Color', [0.3010 0.7450 0.9330]);
x = carState7(:,1);
y = carState7(:,2);
stairs(x,y, '-o', 'Color', [0.6350 0.0780 0.1840]);
x = carState8(:,1);
y = carState8(:,2);
stairs(x,y, '-o', 'Color', [0 0 0]);

legend('Car 1 (0x6a40)', 'Car 2 (0x6743)', 'Car 3 (0x673b)', 'Car 4 (0x6a1a)', 'Car 5 (0x5555)', 'Car 6 (0x6666)', 'Car 7 (0x7777)', 'Car 8 (0x8888)');

hold off;
