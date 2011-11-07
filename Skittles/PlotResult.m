function PlotResult(n)
colorNum = 5;
dataTypeNum = 4;

playerLog = load( strcat('P',num2str(n),'.txt') );
if ( size( playerLog, 2 ) ~= colorNum * dataTypeNum + 1 )
    disp( 'Wrong' );
end
roundNum = size( playerLog, 1 );
figure;
for dataTypeIndex = 1 : dataTypeNum
    subplot( dataTypeNum + 1, 1, dataTypeIndex );
    grid( gca, 'minor' );
    xlim( [ 0, roundNum] );
    hold on;
    playerCols = ( dataTypeIndex - 1 ) * colorNum + 1 : dataTypeIndex * colorNum;
    bar( playerLog( :, playerCols ) );
end
subplot( dataTypeNum + 1, 1, dataTypeNum + 1 );
grid( gca, 'minor' );
xlim( [ 0, roundNum] );
hold on;
plot( playerLog( :, dataTypeNum * colorNum + 1 ) )
