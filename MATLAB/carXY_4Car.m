csvfile_6a40 = readtable('0x6a40_CarTracker.csv');
csvfile_6743 = readtable('0x6743_CarTracker.csv');
csvfile_673b = readtable('0x673b_CarTracker.csv');
csvfile_6a1a = readtable('0x6a1a_CarTracker.csv');

csvfile_collisions = readtable('Collisions.csv');

car_6a40 = table2array(csvfile_6a40(1:end,4:5));
car_6743 = table2array(csvfile_6743(1:end,4:5));
car_673b = table2array(csvfile_673b(1:end,4:5));
car_6a1a = table2array(csvfile_6a1a(1:end,4:5));

collisions = table2array(csvfile_collisions(1:end,4));
collisions = [collisions table2array(csvfile_collisions(1:end,5))];
collisions = [collisions table2array(csvfile_collisions(1:end,6))];
collisions = [collisions table2array(csvfile_collisions(1:end,9))];
collisions = [collisions table2array(csvfile_collisions(1:end,10))];
collisions = [collisions table2array(csvfile_collisions(1:end,14))];


%collisions = transpose(collisions);

trackdimen = [0 5200];
crossdimen = [1960 3200];

car1 = transpose(car_6a40);
car2 = transpose(car_6743);
car3 = transpose(car_673b);
car4 = transpose(car_6a1a);

figure();

x = car1(1,1:end);
y = car1(2,1:end);
plot(x,y, '-o', 'linewidth', 1);
xlim(trackdimen);
ylim(trackdimen);

%xlim([1960 3200]);
%ylim([1960 3200]);

hold on;
x = car2(1,1:end);
y = car2(2,1:end);
plot(x,y, '-o',  'linewidth', 1);

x = car3(1,1:end);
y = car3(2,1:end);
plot(x,y, '-o',  'linewidth', 1);

x = car4(1,1:end);
y = car4(2,1:end);
plot(x,y, '-o',  'linewidth', 1);



track = imread('track_5200x5200.jpg');
h = image(xlim,ylim,track);
%h = image(xlim,ylim,crossroad);
uistack(h,'bottom');
pbaspect([1 1 1]);
xlabel('X Coordinate (mm)');
ylabel('Y Coordinate (mm)');

hold off;
%legend('Car 1 (0x6a40)', 'Car 2 (0x6743)', 'Car 3 (0x673b)', 'Car 4 (0x6a1a)');
legend('Car 1 (0x6a40)', 'Car 2 (0x6743)', 'Car 3 (0x673b)', 'Car 4 (0x6a1a)');
title('Car Paths');

figure();
x = car1(1,1:end);
y = car1(2,1:end);
plot(x,y, '-o', 'linewidth', 1);

xlim(crossdimen);
ylim(crossdimen);

hold on;
x = car2(1,1:end);
y = car2(2,1:end);
plot(x,y, '-o',  'linewidth', 1);

x = car3(1,1:end);
y = car3(2,1:end);
plot(x,y, '-o',  'linewidth', 1);

x = car4(1,1:end);
y = car4(2,1:end);
plot(x,y, '-o',  'linewidth', 1);

track = imread('track_5200x5200.jpg');
crossroad = track(1961:3200, 1961:3200, 1:end);
%h = image(xlim,ylim,track);
h = image(xlim,ylim,crossroad);
uistack(h,'bottom');
pbaspect([1 1 1]);
xlabel('X Coordinate (mm)');
ylabel('Y Coordinate (mm)');

hold off;
%legend('Car 1 (0x6a40)', 'Car 2 (0x6743)', 'Car 3 (0x673b)', 'Car 4 (0x6a1a)');
legend('Car 1 (0x6a40)', 'Car 2 (0x6743)', 'Car 3 (0x673b)', 'Car 4 (0x6a1a)');
title('Car Paths (inside Crossroad)');


figure(); %collisions graph
title('Distances between Cars');
collisionDistance = zeros(size(collisions)) + 300;
hold on;
plot(collisions, '-');
plot(collisionDistance, '--', 'linewidth', 2);
xlabel('Sample');
ylabel('Distance Between Cars (mm)');
hold off;
legend('Car 1 - Car 2', 'Car 1 - Car 3', 'Car 1 - Car 4', 'Car 2 - Car 3', 'Car 2 - Car 4', 'Car 3 - Car 4', 'Collision distance');