%csvfile_6a40 = readtable('0x6a40_CarTracker.csv');
%csvfile_6743 = readtable('0x6743_CarTracker.csv');
%csvfile_673b = readtable('0x673b_CarTracker.csv');
%csvfile_6a1a = readtable('0x6a1a_CarTracker.csv');

car_6a40 = table2array(csvfile_6a40(1:end,13:14));
car_6743 = table2array(csvfile_6743(1:end,13:14));
car_673b = table2array(csvfile_673b(1:end,13:14));
car_6a1a = table2array(csvfile_6a1a(1:end,13:14));

car1 = transpose(car_6a40);
car2 = transpose(car_6743);
car3 = transpose(car_673b);
car4 = transpose(car_6a1a);

figure()
xrange =  [0 5000];
yrange = [0 1.5];


x = car1(1, 1:end);
y = car1(2, 1:end);
plot(x,y, '-o');
hold on;
x = car2(1, 1:end);
y = car2(2, 1:end);
plot(x,y, '-o');
x = car3(1, 1:end);
y = car3(2, 1:end);
plot(x,y, '-o');
x = car4(1, 1:end);
y = car4(2, 1:end);
plot(x,y,'-o');
xlim(xrange);
ylim(yrange);
legend('Car 1 (0x6a40)', 'Car 2 (0x6743)', 'Car 3 (0x673b)', 'Car 4 (0x6a1a)');
xlabel('Time Stamp (ms)');
ylabel('Reservation');
hold off;
