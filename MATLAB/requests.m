csvState = readtable('CarData_CarState_0x6a40.csv');

carState = table2array(csvState(1:end, 2))/1000000;
carState = cat(2, carState, table2array(csvState(1:end, 12)));

csv_queue = readtable('IntersectionQueue.csv');

csvTracker = readtable('0x6a40_CarTracker.csv');
tracker = table2array(csvTracker(1:end, 2))/1000000;
res = strcmp(table2array(csvTracker(1:end, 10)), 'true');
tracker = cat(2, tracker, res);


time = table2array(csv_queue(1:end,2))/1000000;
ids = char(table2array(csv_queue(1:end,3)));
ids_val = hex2dec(ids(:,3:end));
reservation = table2array(csv_queue(1:end,4));
reservation = (reservation *2)-1;
throttle = table2array(csv_queue(1:end,9));
speed = table2array(csv_queue(1:end,8));

entries = cat(2, time, ids_val, reservation, throttle, speed);

minTime = min([entries(1,1), carState(1,1), tracker(1,1)]);
entries(:,1) = entries(:,1) - minTime;
carState(:,1) = carState(:,1) - minTime;
tracker(:,1) = tracker(:,1) - minTime;

entry_6a40 = entries(entries(:,2) == hex2dec('6a40'), :);
entry_6a40(entry_6a40(:,2) == 0) = -1; 



figure();
xrange =  [0 5000];
yrange = [-1 1];

hold on;
x = entry_6a40(:,1);
yyaxis left;
ylabel('Reservation Request (1 = Approved, -1 = Denied)');
y = entry_6a40(:,3);
stem(x,y);

yyaxis right;
ylabel('Car Speed (mm/s)');
x = carState(:,1);
y = carState(:,2);
stairs(x,y, '-o');
xlim(xrange);
%ylim(yrange);
hold off;

figure()
xrange =  [0 5000];
yrange = [-1 1];

hold on;
xlabel('Time (ms)')
x = tracker(:,1);
yyaxis left;
ylabel('Has approved Reservation (1 = true, 0 = false)');
y = tracker(:,2);
stairs(x,y, '-o');

yyaxis right;
ylabel('Car Speed (mm/s)');
x = carState(:,1);
y = carState(:,2);
stairs(x,y, '-o');
xlim(xrange);
%ylim(yrange);
hold off;
